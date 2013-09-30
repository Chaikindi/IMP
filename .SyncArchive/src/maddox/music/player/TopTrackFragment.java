package maddox.music.player;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.*;

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

/**
 * Created by maddox on 30.06.13.
 */
public class TopTrackFragment  extends Fragment
{
    String artist;
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

        artist = settings.getString("LastTrackName",null);

        if(artist != null)
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
        String fileName = "0:"+artist+".dat";

        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("typeLast", 0);
        editor.putString("LastTrackName", artist);
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

        for(int i = 0; i < 100; i++)
        {
            String[] s = str[i].split(":");
            MadPlayer.Track tr = new MadPlayer.Track();
            tr.artist = s[0];
            tr.track = s[1];
            list.add(tr);
        }

        if(player != null)
        {
            player.clearPlayList();
            player.addPlayList(list, accessToken, controls.playedText, controls.songView, controls.seekBar,0);
        }
        controls.setPA(player,player.adapter);

        return 1;
    }
    class DownloadAndParseXML extends AsyncTask<String, Void, ArrayList<MadPlayer.Track>>
    {
        @Override
        protected ArrayList<MadPlayer.Track> doInBackground(String... params)
        {
            ArrayList<MadPlayer.Track> list = null;
            artist = params[0].replaceAll(" ","%20");
            String LastFmUrl = "http://ws.audioscrobbler.com/2.0/?method=artist.gettoptracks&artist="+artist+"&limit=100&api_key=d5a78892a7f4dfa1836eaccd82a5cea1";
            xmlWork xmlwork = new xmlWork();
            String xml  = xmlwork.getXmlFromUrl(LastFmUrl);

            if(xml != null && xml.compareTo("")!= 0)
            {
                Document doc = xmlwork.getDomElement(xml);
                NodeList nl = doc.getElementsByTagName("track");
                NodeList nl2 = doc.getElementsByTagName("artist");
                list = new ArrayList<MadPlayer.Track>();

                for (int i = 0; i < nl.getLength(); i++)
                {
                    MadPlayer.Track track = new MadPlayer.Track();
                    Element e1 = (Element)nl.item(i);
                    Element e2 = (Element)nl2.item(i);
                    track.track = xmlwork.getValue(e1, "name");
                    track.artist = xmlwork.getValue(e2, "name");
                    list.add(track);
                }
            }
            else
            {
                LastFmUrl = "http://www.musicbrainz.org/ws/2/recording/?&query=artist:%22"+artist+"%22";
                xmlwork = new xmlWork();
                xml  = xmlwork.getXmlFromUrl(LastFmUrl);
                if(xml != null && xml.compareTo("")!= 0)
                {
                    Document doc = xmlwork.getDomElement(xml);
                    NodeList nl = doc.getElementsByTagName("track");
                    NodeList nl2 = doc.getElementsByTagName("artist");
                    list = new ArrayList<MadPlayer.Track>();

                    for (int i = 0; i < nl.getLength(); i++)
                    {
                        MadPlayer.Track track = new MadPlayer.Track();
                        Element e1 = (Element)nl.item(i);
                        Element e2 = (Element)nl2.item(i);
                        track.track = xmlwork.getValue(e1, "name");
                        track.artist = xmlwork.getValue(e2, "name");
                        list.add(track);
                    }
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

            //frag.adapter = new PlayListAdapter(frag.controls.songView.getContext(),R.layout.text, tackList, -1);
            player.clearPlayList();
            player.addPlayList(list, accessToken,controls.playedText,controls.songView,controls.seekBar,1);
            //frag.player.getAllTrackUrl();
            //frag.controls.songView.setAdapter(frag.adapter);
            controls.setPA(player,player.adapter);
            controls.songView.setVisibility(View.VISIBLE);

            try
            {
                savePlayList(tackList);
            }
            catch (IOException ignored) {}

            SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("typeLast", 0);
            editor.putString("LastTrackName", artist);
            // Commit the edits!
            editor.commit();
        }
    }
    public void searchArtistTopTrack(final String artist)
    {
        DownloadAndParseXML dow = new DownloadAndParseXML();
        if(controls !=null)
            if(controls.songView != null)
                controls.songView.setVisibility(View.INVISIBLE);
        dow.execute(artist);
    }
    void savePlayList(ArrayList<String> tackList ) throws IOException
    {
        String fileName = "0:"+artist+".dat";

        FileOutputStream fos;

        fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

        for(int i = 0; i <tackList.size(); i++)
        {
            String tr = tackList.get(i);
            String w = tr+"/-/";

            fos.write(w.getBytes());
        }

        fos.close();
    }
}
