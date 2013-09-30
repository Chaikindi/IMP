package maddox.music.player;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class FavoriteListFragment extends Fragment
{
    ArrayList<MadPlayer.Track> favoriteList;
    MadPlayer player;
    Controls controls;
    String accessToken;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        favoriteList = new ArrayList<MadPlayer.Track>();
        View v = inflater.inflate(R.layout.new_main, null);
        loadPref();
        controls = new Controls();
        v = controls.LoadControls(v);
        player = new MadPlayer();
        try {
            loadPlayList();
            setPlayList();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return v;
    }

    void loadPref()
    {
        SharedPreferences settings = getActivity().getSharedPreferences("MadPlayerPreferences", 0);
        accessToken  = settings.getString("accessToken", null);
    }

    void saveFavoriteList(ArrayList<MadPlayer.Track> tackList ) throws IOException
    {
        String fileName = "Favorite.dat";

        FileOutputStream fos;

        fos = getActivity().openFileOutput(fileName, Context.MODE_PRIVATE);

        for (MadPlayer.Track tr : tackList)
        {
            String w = tr.artist + ":" + tr.track + "/-/";

            fos.write(w.getBytes());
        }

        fos.close();
    }

    int loadPlayList() throws IOException
    {
        String fileName = "Favorite.dat";

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
            favoriteList  = list;
        }
        return 1;
    }
    void setPlayList()
    {
        if(player != null)
        {
            player.clearPlayList();
            player.addPlayList(favoriteList, accessToken, controls.playedText, controls.songView, controls.seekBar,0);
        }
        controls.setPA(player,player.adapter);
    }


    void addToFavorite(MadPlayer.Track track)
    {
        if(favoriteList != null)
        {
            favoriteList.add(track);

            try {
                saveFavoriteList(favoriteList);
            } catch (IOException e) {
                e.printStackTrace();
            }
            setPlayList();
        }
    }
    void deleteToFavorite(int i)
    {
        if(favoriteList != null)
            favoriteList.remove(i);

        try {
            saveFavoriteList(favoriteList);
        } catch (IOException e) {
            e.printStackTrace();
        }
        setPlayList();
    }
}