package maddox.music.player;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.SeekBar;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

public class MadPlayer {//extends Service {

	static class Track
	{
		String artist;
		String url;
		String track;
		int duration;
		int n;
	}
    static class Album
    {
        String name;
        String artist;
        Drawable image;
        String year;
        String count;
        String url;
    }
	
	private final IBinder binder = new MyBinder();
	String accessToken;
	MediaPlayer mp;
	int curentTrack = -1;
	int playerStat; // 0 - stop; 1 - play; 2 - pause;
	PlayListAdapter adapter;
    SeekBar seekBar;
	TextView text;
	int randomPlaying = 0;
    int repared = 0;
    ListView lv;
	xmlWork xmlwork;

	ArrayList<Track> playList;
    ArrayList<Track> playListTrack;
    ArrayList<Track> playListTag;
    PlayListAdapter adapterTrack;
    PlayListAdapter adapterTag;

    DownloadUrlTrack dow;
    WaitUrl wu;

	
	
	//public void onCreate() {
	//    super.onCreate();
	MadPlayer()
    {
	    playList = new ArrayList<Track>();
        playListTrack = new ArrayList<Track>();
        playListTag = new ArrayList<Track>();
	    mp = new MediaPlayer();
		xmlwork = new xmlWork();
		mp.setOnPreparedListener(mpPrepareListener);
		mp.setOnBufferingUpdateListener(mpBufferingUpdateListener);
		mp.setOnCompletionListener(mpCompletionListener);
        mp.setOnSeekCompleteListener(seekCompleteListener);
		
		Log.d("S", "MyService onCreate");
	 }
	
//	@Override
//	public IBinder onBind(Intent intent) {
//		// TODO Auto-generated method stub
//		Log.d("S", "MyService onBind");
//		return binder;
//	}
	
	public class MyBinder extends Binder {
		MadPlayer getService() {
	      return MadPlayer.this;
	    }
	  }

	void addPlayList (String artist, ArrayList<String> list, String access, PlayListAdapter adapter, TextView text, ListView lv, SeekBar seekBar)
	{
		//playList = new ArrayList<Track>();
		for(int i = 0; i < list.size(); i++)
		{
			Track track = new Track();
			
			String[] str = list.get(i).split(":");
			
			track.artist = str[0];
			track.track = str[1];
			track.n = i;
			playList.add(track);			
		}		
		this.adapter = adapter;		
		accessToken = access;
		this.text = text;
        this.lv = lv;
        this.seekBar = seekBar;
	}

    void setPlayList(int index)
    {
        if(index == 0)
            playList = playListTrack;
        if(index == 1)
            playList = playListTag;

        if(mp != null)
            mp.reset();
        curentTrack = -1;
    }
	
	void addPlayList (ArrayList<Track> list, String access, TextView text, ListView lv, SeekBar seekBar, int index)
	{
        ArrayList<String> tackList = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
		{
			Track track = new Track();

			track.artist = list.get(i).artist;
			track.track = list.get(i).track;
			track.n = i;
			playList.add(track);
            if(index == 0)
                playListTrack.add(track);
            if(index == 1)
                playListTag.add(track);

            String ss = track.artist +":"+ track.track;
            tackList.add(ss);
		}

		//this.adapter = adapter;
		accessToken = access;
		this.text = text;
        this.lv = lv;
        adapter  = new PlayListAdapter(lv.getContext(),R.layout.text, tackList, -1);
        this.lv.setAdapter(adapter);
        this.seekBar = seekBar;
	}
	
	void clearPlayList()
	{
		if(playList != null)
			playList.clear();
        if(lv != null)
            lv.setVisibility(0);

	}
    void setAdapter(PlayListAdapter adapter)
    {
        this.adapter = adapter;
    }
	
	void setCurrentTrack(int n)
	{
		curentTrack = n;
        lv.smoothScrollToPosition(n);
	}
	
	int getCurrentTrack()
	{
		return curentTrack;
	}
	
	void setPlayerStat(int n)
	{
		playerStat = n;
		if(adapter != null)
			adapter.setPlay(n);
		Log.d("Log", "Player stat: "+playerStat);
	}
	void getAllTrackUrl()
	{
		DownloadAllUrlTrack dow = new DownloadAllUrlTrack();
		//dow.execute(playList);
//        for(int i = 0; i < playList.size(); i++)
//        {
//            getTrackInfo(playList.get(i));
//        }
	}
    void setPause()
    {
        if(mp != null)
            mp.pause();
    }
	
	int getPlayerStat()
	{
		return playerStat;
	}
	
	boolean isPlaying()
	{
		if(mp != null)
			return mp.isPlaying();
		
		return false;			
	}
	public String getUrl(int n)
	{
		Track track = playList.get(n);
		String q = track.artist+"%20"+track.track;
		q = q.replaceAll(" ", "%20");  
		return "https://api.vk.com/method/audio.search.xml?q="+q+"&count=1&access_token="+accessToken;
	}
	void getTrackInfo(Track track, int n, int play)
	{
        if(track.url == null || track.url.compareTo("")==0)
        {
            if(dow != null)
            {
                AsyncTask.Status stat = dow.getStatus();
                if(AsyncTask.Status.RUNNING  == stat)
                {
                    dow.cancel(true);
                }
            }

            dow = new DownloadUrlTrack();
            dow.execute(track);
        }

        if(wu != null)
        {
            AsyncTask.Status stat = wu.getStatus();
            if(AsyncTask.Status.RUNNING  == stat)
            {
                wu.cancel(true);
            }
        }

        wu = new WaitUrl();
        wu.execute(new Integer[]{n,play});
	}

	public void addToPlayList(String artist, String trackName)
	{
		Track track = new Track();
		track.artist  = artist;
		track.track  = trackName;
		
		playList.add(track);		
	}

	public void PlayTrack(int n)
	{
		Track track = playList.get(n);

		if(track.url  == null)
		{				
			getTrackInfo(track,n,1);
		}
		else
		{
			if(getCurrentTrack() != n || repared == 1)
			{
				setCurrentTrack(n);

				if(mp != null)
					mp.reset();


				if(track != null)
				{
					try {
						if(track.url  != null)
							mp.setDataSource(track.url);

					} catch (IllegalArgumentException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IllegalStateException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					Log.d("Log", "Start prepare");

					//prepareText.setText("Start buffering...");
					mp.prepareAsync();
					setPlayerStat(1);
					text.setText(track.artist +" - "+ track.track);
					adapter.setPos(getCurrentTrack());
					adapter.notifyDataSetChanged();
				}
				else
				{
					PlayNextTrack(1);
				}

			}
			else
			{
				PauseTrack();
			}
		}

	}
	
	public void StopTrack()
	{
		if(mp != null)
    	{
			mp.stop();
			setPlayerStat(0);
    	}
	}
	
	public void PauseTrack()
	{
		if(getPlayerStat() != 2)
		{
			if(mp != null)
			{
				mp.pause();
				setPlayerStat(2);
			}
		}
		else
		{
			if(mp != null)
			{
				mp.start();
				setPlayerStat(1);
			}
		}
	}
	
	public void PlayNextTrack(int i) //i == 0 replayTrack; i == 1 nextTrack; i == 2 prevTrack;
	{
		int n  = getCurrentTrack();
		if(i != 0 && repared == 0)
		{
			if(randomPlaying == 0)
			{
				if(i == 1)
				{
					if(n < playList.size()-1)  
					{
						n++;
					}
					else 
					{
						n = 0;
					}
				}
				else 
				{
					if( n > 0 )
					{
						n--;
					}
					else
					{ 
						n = playList.size()-1; 
					}
				}
			}
			else if(randomPlaying == 1)
			{
				Random rand = new Random();
				n = rand.nextInt(playList.size());
				if(n > playList.size()-1) n = 0; 
				
			}
			adapter.setPos(n);
			adapter.notifyDataSetChanged();
		}
		StopTrack();
		PlayTrack(n);			
	}
	
	OnInfoListener mpInfoListener = new OnInfoListener(){

		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			// TODO Auto-generated method stub
			return false;
		}};
	
	OnCompletionListener mpCompletionListener = new OnCompletionListener(){

		public void onCompletion(MediaPlayer mp) {
			// TODO Auto-generated method stub
            adapter.setDuration(0, 0, 0);
			
			if(repared == 0)
               PlayNextTrack(1);
            else if(repared == 1)
            {
                PlayNextTrack(1);
            }
            //lv.smoothScrollToPosition(getCurrentTrack());
			
		}};

	OnPreparedListener mpPrepareListener = new OnPreparedListener(){

		public void onPrepared(MediaPlayer mp) {
			// TODO Auto-generated method stub
			mp.start();				
			Log.d("Log", "Start play");

		}};
		
    void setCurrentDuration(int currentDuration)
    {
        int min, sek;

        min  = (currentDuration/1000)/60;
        sek  = (currentDuration/1000) - min*60;
        String mstr = min+":";
        String sstr = "";

        if(sek < 10)
            sstr = "0"+sek;
        else
            sstr += sek;

        text.setText(mstr+""+sstr);
    }

    void setSeek(int to)
    {
        int n = getCurrentTrack();
        Track track = playList.get(n);
        float  dur = track.duration;
        dur = dur/100;
        float seekTo = dur * to * 1000;
        if(mp.isPlaying())
            mp.seekTo((int)seekTo);
    }
    MediaPlayer.OnSeekCompleteListener seekCompleteListener = new MediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(MediaPlayer mediaPlayer) {
            Log.d("MP","Seek Complete. Current Position: " + mp.getCurrentPosition());
            //mp.start();
        }
    };

	OnBufferingUpdateListener mpBufferingUpdateListener = new OnBufferingUpdateListener()
    {
        public void onBufferingUpdate(MediaPlayer mp, int percent)
        {

            int n = getCurrentTrack();
            Track track = playList.get(n);

            adapter.setDuration(mp.getCurrentPosition(), track.duration, percent);
            //seekBar.setMax((int)track.duration);
            float dur  = track.duration;
            dur = dur / 100;
            float cur = mp.getCurrentPosition();
            float curPos = (cur/1000)/dur;
            seekBar.setProgress((int)curPos);
            //float secondary  = (track.duration/100) * percent;
            //seekBar.setSecondaryProgress((int)secondary);
        }
    };
			
	private String getValue(Element item, String str) 
	{
		NodeList n = item.getElementsByTagName(str);
		return this.getElementValue(n.item(0));
	}

	private final String getElementValue( Node elem ) 
	{
		Node child;
		if( elem != null){
			if (elem.hasChildNodes()){
				for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
					if( child.getNodeType() == Node.TEXT_NODE  ){
						return child.getNodeValue();
					}
				}
			}
		}
		return "";
	}

			
	class DownloadUrlTrack extends AsyncTask<Track, Void, Track> 
	{
		Track track1;
		@Override
		protected Track doInBackground(Track... params) {
			// TODO Auto-generated method stub
			Track track = params[0];
			track1 = params[0];
			String q = track.artist.replaceAll(" ","%20")+"%20"+track.track.replaceAll(" ","%20");
			q = q.replaceAll(" ", "%20");
            q = q.replaceAll("\\x22", "");
            Log.d("URL","Start load url "+track.artist+" "+ track.track);
			String vpApiUrl  = "https://api.vk.com/method/execute.getUrl.xml?artist="+track.artist.replaceAll(" ","%20")+"&track="+track.track.replaceAll(" ","%20")+"&access_token="+accessToken;
			//String vpApiUrl  = "https://api.vk.com/method/audio.search.xml?q="+q+"&count=1000&auto_complete=1&access_token="+accessToken;
			
			String xml = xmlwork.getXmlFromUrl(vpApiUrl);
			char car = xml.charAt(0);
			if(car != '<')
			{
				xml = xml.substring(1);
			}
			if( xml != null)
			{
				Document doc = xmlwork.getDomElement(xml);         
				//NodeList nl = doc.getElementsByTagName("audio");
                NodeList nl = doc.getElementsByTagName("response");

	
				if(nl.getLength() == 0)
				{
					return null;
				}
				else
				{
                    int minN = 0;
                    Element e = (Element)nl.item(minN);
                    String tName = getValue(e,"artist");
                    String tTrack = getValue(e,"title");
                    String output = "Last: "+ track.artist+" VK: "+tName+ " Compare: "+tName.compareToIgnoreCase(track.artist);
                    String output1 = "Last: "+ track.track+" VK: "+tTrack+ " Compare: "+tTrack.compareToIgnoreCase(track.track);
                    Log.d("Test final: artist name ",output);
                    Log.d("Test final: track name ",output1);

					e = (Element)nl.item(minN);
					if(track.url == null)
						track.url = getValue(e, "url");
					if(track.duration <= 0)
						track.duration = Integer.parseInt(getValue(e, "duration"));
				}
			}
			return track;	
		}
		
		@Override
		protected void onPostExecute(Track track) {
			super.onPostExecute(track);
		
			
//			if(track != null)
//				PlayTrack(track.n);
//			else
//			{
//				setCurrentTrack(track1.n);
//				//PlayNextTrack(1);
//			}
			//Toast.makeText(adapter.getContext(), xml, Toast.LENGTH_LONG).show();
		}
	
	}
	class DownloadAllUrlTrack extends AsyncTask<ArrayList<Track>, Void, Track> 
	{				
		@Override
		protected Track doInBackground(ArrayList<Track>... params)
        {
			// TODO Auto-generated method stub
		    ArrayList<Track> track = params[0];
            ArrayList<Track> ret_track = new ArrayList<Track>();
            ArrayList<String> list = new ArrayList<String>();// = new List<String>();
            Track tr = null;

            for(int i = 0; i<track.size(); i++)
            {
                if(i == 99)
                {
                    int stop = 0;
                    stop += 10;
                }
                Track t = track.get(i);
                String q = t.artist+"%20"+t.track;
                q = q.replaceAll(" ", "%20");
                q = q.replaceAll("\\x22", "");
                String vpApiUrl  = "https://api.vk.com/method/audio.search.xml?q="+q+"&count=1&access_token="+accessToken;

                String xml = xmlwork.getXmlFromUrl(vpApiUrl);
                char car = xml.charAt(0);
                char car2 = '<';
                if(car != '<')
                {
                    xml = xml.substring(1);
                }
                if( xml != null)
                {
                    Document doc = xmlwork.getDomElement(xml);
                    NodeList nl = doc.getElementsByTagName("audio");

                    if(nl.getLength() == 0)
                    {
                        //return null;
                    }
                    else
                    {
                        Element e = (Element)nl.item(0);
                        if(t.url == null)
                            t.url = getValue(e, "url");
                        if(t.duration <= 0)
                            t.duration = Integer.parseInt(getValue(e, "duration"));

                        //Log.d("URL",t.artist+" "+t.track+" "+t.url);
                        //Log.d("URL",t.url);
                    }
                }

                //if(t.url != null)
                {
                    ret_track.add(t);
                    list.add(t.artist+":"+t.track);
                    Log.d("URL",t.artist+" "+t.track+" "+t.url+" "+i);
                }
		    }
				
            playList = ret_track;
            adapter.items = list;


            return tr;
		}
		@Override
		protected void onPostExecute(Track track) 
		{
			super.onPostExecute(track);
			adapter.notifyDataSetChanged();
				
		}		
	}
    class WaitUrl extends AsyncTask<Integer, Void, Void>
    {
        int n = -1;
        @Override
        protected Void doInBackground(Integer... i)
        {
            int ret = 0;
            while (ret == 0)
            {
                Track track = playList.get(i[0]);
                if(track.url != null )//|| track.url.compareTo("") != 0)
                {
                    if(i[1] == 1)
                    {
                      n = i[0];
                    }
                    ret = 1;
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void track) {
            super.onPostExecute(track);

            if(n > -1)
                PlayTrack(n);
        }
    }
}
