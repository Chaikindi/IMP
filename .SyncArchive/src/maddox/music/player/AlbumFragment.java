package maddox.music.player;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

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
 * Created by maddox on 03.07.13.
 */
public class AlbumFragment extends Fragment
{
    String artist;
    MadPlayer player;
    String accessToken;
    Controls controls;
    AlbumTracksFragment frag3;


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

        artist = settings.getString("LastAlbumsArtistName",null);

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
        String fileName = "2:"+artist+".dat";

        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("typeLast", 0);
        editor.putString("LastAlbumsArtistName", artist);
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
        ArrayList<MadPlayer.Album> list = new ArrayList<MadPlayer.Album>();

        for(int i = 0; i < str.length; i++)
        {
            String[] s = str[i].split(":");
            MadPlayer.Album al = new MadPlayer.Album();
            al.artist = s[0];
            al.name = s[1];
            list.add(al);
        }
        AlbumListAdapter adapter  = new AlbumListAdapter(getActivity().getBaseContext(),R.layout.grid_album_item,list);
        controls.songView.setAdapter(adapter);
        controls.songView.setOnItemClickListener(albumClickListener);

        return 1;
    }

    AdapterView.OnItemClickListener albumClickListener =  new AdapterView.OnItemClickListener(){

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            TextView tv = (TextView)view.findViewById(R.id.albumNameTextView);
            Log.d("TEXT:", tv.getText().toString());


            FragmentTransaction fTrans = getFragmentManager().beginTransaction();
            if(frag3 == null)
            {
                frag3 = new AlbumTracksFragment();
                fTrans.add(R.id.linear,frag3);
            }
            else
            {
                fTrans.show(frag3);
            }
            if(AlbumFragment.this !=null)
                fTrans.hide(AlbumFragment.this);
            fTrans.commit();
            frag3.searchAlbumTrack(tv.getText().toString(),artist);
        }
    };
    class DownloadTopArtisAlbums extends AsyncTask<String, Void, ArrayList<MadPlayer.Album>>
    {
        @Override
        protected ArrayList<MadPlayer.Album> doInBackground(String... params)
        {
            Log.d("Info:","DownloadArtistAlbums start!");
            ArrayList<MadPlayer.Album> list = null;

            //artist = params[0];
            String ar = params[0].replaceAll(" ","%20");
            String LastFmUrl = "http://www.musicbrainz.org/ws/2/release/?limit=100&query=artist:%22"+ar+"%22";

            xmlWork xmlwork = new xmlWork();
            String xml  = xmlwork.getXmlFromUrl(LastFmUrl);

            if(xml != null)
            {
                Document doc = xmlwork.getDomElement(xml);
                NodeList nl1 = doc.getElementsByTagName("release");
                NodeList nl2 = doc.getElementsByTagName("release-group");


                list = new ArrayList<MadPlayer.Album>();


                for(int i = 0; i < nl1.getLength(); i++)
                {
                    MadPlayer.Album track = new MadPlayer.Album();
                    Element e1 = (Element)nl1.item(i);
                    Element e12 = (Element)nl2.item(i);
                    String type = e12.getAttribute("type");

                    if(type.compareTo("Live") != 0)
                    {
                        track.name = xmlwork.getValue(e1, "title");
                        track.artist = ar;
                        track.image = null;

                        boolean save = true;
                        for(int j = 0; j < list.size(); j++)
                        {
                            MadPlayer.Album t = list.get(j);
                            if(t.name.compareTo(track.name)  == 0)
                            {
                                save = false;
                                break;
                            }
                        }
                        if(save)
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
        protected void onPostExecute(ArrayList<MadPlayer.Album> list)
        {
            super.onPostExecute(list);
            AlbumListAdapter adapter  = new AlbumListAdapter(getActivity().getBaseContext(),R.layout.grid_album_item,list);
            controls.songView.setAdapter(adapter);
            controls.songView.setOnItemClickListener(albumClickListener);
            controls.songView.setVisibility(View.VISIBLE);
            SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("typeLast", 0);
            editor.putString("LastAlbumsArtistName", artist);
            // Commit the edits!
            editor.commit();

            try {
                savePlayListAlbum(list);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void searchArtistAlbums(String a)
    {
        DownloadTopArtisAlbums dow = new DownloadTopArtisAlbums();
        if(controls !=null)
            if(controls.songView != null)
                controls.songView.setVisibility(View.INVISIBLE);
        dow.execute(a);
        artist = a;
    }
    void savePlayList(ArrayList<String> tackList ) throws IOException
    {
        String fileName = "3:"+artist+".dat";

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
    void savePlayListAlbum(ArrayList<MadPlayer.Album> tackList ) throws IOException
    {
        String fileName = "2:"+artist+".dat";

        FileOutputStream fos;

        fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

        for(int i = 0; i <tackList.size(); i++)
        {
            MadPlayer.Album tr = tackList.get(i);
            String w = tr.artist+":"+tr.name+"/-/";

            fos.write(w.getBytes());
        }

        fos.close();
    }
}