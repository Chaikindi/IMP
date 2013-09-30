package maddox.music.player;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
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

import maddox.music.player.MadPlayer.Track;

/**
 * Created by maddox on 30.06.13.
 */
public class TopTagFragment extends Fragment
{
    String tag;
    PlayListAdapter adapter;
    MadPlayer player;
    private MadPlayer.MyBinder ser;
    String accessToken;
    Controls controls;
    ArrayList<Track> playList;


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

    @Override
    public void onStart()
    {
        super.onStart();
//        Intent intent = new Intent(getActivity(), MadPlayer.class);
//        getActivity().bindService(intent, sConn, Context.BIND_AUTO_CREATE);
//        getActivity().startService(intent);



    }
    @Override
    public void onAttach (Activity activity)
    {
        super.onAttach(activity);
        int stop = 0;
    }
    void setPlayList()
    {
//        player.setPlayList(1);
//        player.setAdapter(adapter);
    }
    void loadPref()
    {
        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        accessToken  = settings.getString("accessToken", null);
    }

    private ServiceConnection sConn = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            ser = (MadPlayer.MyBinder) service;
            player = ser.getService();
            lastPlayList();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {

        }
    };

    void lastPlayList()
    {
        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);

        tag = settings.getString("LastTagName",null);

        if(tag != null)
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
        String fileName = "1:"+tag+".dat";

        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("typeLast", 1);
        editor.putString("LastTagName", tag);
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
        String input = null;
        while((content = br.read()) != -1)
        {
            if(input == null)
            {
                input = "";
            }
            input += (char)content;
        }
        if(input == null)
        {
           return 0;
        }

        String[] str = input.split("/-/");
        ArrayList<MadPlayer.Track> list = new ArrayList<MadPlayer.Track>();
        //ArrayList<String> tackList = new ArrayList<String>();

        for(int i = 0; i < 100; i++)
        {
            String[] s = str[i].split(":");
            MadPlayer.Track tr = new MadPlayer.Track();
            tr.artist = s[0];
            tr.track = s[1];
            //String ss = tr.artist +":"+ tr.track;
            //tackList.add(ss);
            list.add(tr);
        }

        //adapter = new PlayListAdapter(controls.songView.getContext(),R.layout.text, tackList, -1);
        if(player != null)
        {
            player.clearPlayList();
            player.addPlayList(list, accessToken, controls.playedText, controls.songView, controls.seekBar,1);
        }
        //player.getAllTrackUrl();
        //controls.songView.setAdapter(adapter);
        controls.setPA(player,player.adapter);
        //playList = player.playList;

        return 1;
    }

    class DownloadTopTagTrack extends AsyncTask<String, Void, ArrayList<Track>>
    {
        @Override
        protected ArrayList<MadPlayer.Track> doInBackground(String... params)
        {
            ArrayList<MadPlayer.Track> list = null;

            tag = params[0];
            String LastFmUrl = "http://ws.audioscrobbler.com/2.0/?method=tag.gettoptracks&tag="+tag+"&limit=100&api_key=d5a78892a7f4dfa1836eaccd82a5cea1";
            xmlWork xmlwork = new xmlWork();
            String xml  = xmlwork.getXmlFromUrl(LastFmUrl);

            if(xml != null)
            {
                Document doc = xmlwork.getDomElement(xml);
                NodeList nl1 = doc.getElementsByTagName("track");
                NodeList nl2 = doc.getElementsByTagName("artist");
                list = new ArrayList<MadPlayer.Track>();


                for(int i = 0; i < nl1.getLength(); i++)
                {
                    MadPlayer.Track track = new MadPlayer.Track();
                    Element e1 = (Element)nl1.item(i);
                    Element e2 = (Element)nl2.item(i);
                    track.track = xmlwork.getValue(e1, "name");
                    track.artist = xmlwork.getValue(e2, "name");
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
            //frag1.adapter = new PlayListAdapter(frag1.controls.songView.getContext(),R.layout.text, tackList, -1);
            player.clearPlayList();
            player.addPlayList(list, accessToken, controls.playedText,controls.songView,controls.seekBar,1);
            //frag1.player.getAllTrackUrl();
            //frag1.controls.songView.setAdapter(frag1.adapter);
            controls.setPA(player,player.adapter);
            controls.songView.setVisibility(View.VISIBLE);

            try
            {
                savePlayList(tackList);
            }
            catch (IOException ignored) {}

            SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("typeLast", 1);
            editor.putString("LastTagName", tag);
            // Commit the edits!
            editor.commit();



        }
    }
    public void searchTagTopTrack(final String tag)
    {
        DownloadTopTagTrack dow = new DownloadTopTagTrack();
        if(controls !=null)
            if(controls.songView != null)
                controls.songView.setVisibility(View.INVISIBLE);
        dow.execute(tag);
    }
    void savePlayList(ArrayList<String> tackList ) throws IOException
    {
        String fileName = "1:"+tag+".dat";

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
