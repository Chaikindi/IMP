package maddox.music.player;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class AlbumTracksFragment extends Fragment
{
    String nameAlbum;
    MadPlayer player;
    String accessToken;
    Controls controls;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.new_main, null);
        loadPref();
        controls = new Controls();
        v = controls.LoadControls(v);
        player = new MadPlayer();
        lastPlayList();
        return v;
    }

    void loadPref()
    {
        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        accessToken  = settings.getString("accessToken", null);
    }

    void lastPlayList()
    {
        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);

        nameAlbum = settings.getString("LastAlbumName",null);

        if(nameAlbum != null)
        {
            try {
                loadPlayList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    int loadPlayList() throws IOException
    {
        String fileName = "3:"+nameAlbum+".dat";

        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("typeLast", 3);
        editor.putString("LastAlbumName", nameAlbum);
        // Commit the edits!
        editor.commit();

        FileInputStream fos = getActivity().openFileInput(fileName);

        if(fos == null)
        {
            return 0;
        }

        DataInputStream in = new DataInputStream(fos);
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));

        int content;
        String input = "";
        while((content = br.read()) != -1)
        {
            input += (char)content;
        }

        String[] str = input.split("/-/");
        ArrayList<MadPlayer.Track> list = new ArrayList<MadPlayer.Track>();

        if(str.length > 0)
        {
            if(str[0].contentEquals(""))
            {
                MadPlayer.Track tr = new MadPlayer.Track();
                tr.artist = "";
                tr.track = "No Data";
                list.add(tr);
            }
            else
            {
                for (String aStr : str)
                {
                    String[] s = aStr.split(":");
                    MadPlayer.Track tr = new MadPlayer.Track();
                    tr.artist = s[0];
                    tr.track = s[1];
                    list.add(tr);
                }
            }
            if(player != null)
            {
                player.clearPlayList();
                player.addPlayList(list, accessToken, controls.playedText, controls.songView, controls.seekBar,0);
            }
            controls.setPA(player,player.adapter);
        }
        return 1;
    }
    class DownloadAlbumTrack extends AsyncTask<String, Void, ArrayList<MadPlayer.Track>>
    {
        @Override
        protected ArrayList<MadPlayer.Track> doInBackground(String... params)
        {
            Log.d("Info:", "DownloadAlbumTrack start!");
            ArrayList<MadPlayer.Track> list = null;

            String album = params[0].replaceAll(" ", "%20");
            String artist = params[1].replaceAll(" ", "%20");
            String LastFmUrl = "http://www.musicbrainz.org/ws/2/recording/?&query=artist:%22"+artist+"%22%20AND%20release:%22"+album+"%22";
            xmlWork xmlwork = new xmlWork();
            String xml  = xmlwork.getXmlFromUrl(LastFmUrl);

            if(xml != null && xml.compareTo("")!= 0)
            {
                Document doc = xmlwork.getDomElement(xml);
                NodeList nl1 = doc.getElementsByTagName("recording");
                list = new ArrayList<MadPlayer.Track>();


                for(int i = 0; i < nl1.getLength(); i++)
                {
                    MadPlayer.Track track = new MadPlayer.Track();
                    Element e1 = (Element)nl1.item(i);
                    track.track = xmlwork.getValue(e1, "title");
                    track.artist = artist;
                    list.add(track);
                }
            }

            return list;
        }

        @Override
        protected void onProgressUpdate(Void... values)
        {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(ArrayList<MadPlayer.Track> list)
        {
            super.onPostExecute(list);

            ArrayList<String> tackList = new ArrayList<String>();

            if(list != null)
            {
                for (MadPlayer.Track aList : list)
                {
                    String str = aList.artist + ":" + aList.track;
                    tackList.add(str);
                }
            }
            try
            {
                savePlayList(tackList);
            }
            catch (IOException ignored) {}
            player.clearPlayList();
            player.addPlayList(list, accessToken,controls.playedText,controls.songView,controls.seekBar,1);
            controls.setPA(player,player.adapter);
            controls.songView.setOnItemClickListener(controls.trackClickListener);
            controls.songView.setVisibility(View.VISIBLE);
        }
    }
    public void searchAlbumTrack(String albumName, String a)
    {
        DownloadAlbumTrack dow = new DownloadAlbumTrack();
        if(controls !=null)
            if(controls.songView != null)
                controls.songView.setVisibility(View.INVISIBLE);
        dow.execute(albumName, a);
    }
    void savePlayList(ArrayList<String> tackList ) throws IOException
    {
        String fileName = "3:"+nameAlbum+".dat";

        FileOutputStream fos;

        fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

        for (String tr : tackList)
        {
            String w = tr + "/-/";
            fos.write(w.getBytes());
        }

        fos.close();
    }
}