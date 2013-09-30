package maddox.music.player;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class main extends Activity {

	String accessToken, userId, loginIP;
	long expiresIn, loginTime;
	String artist;
    String tag;
	MadPlayer player;
	ActionBar bar;
	SearchView searchView;
	MenuItem searchMenuItem;
    ListView mDrawerList;
    DrawerLayout mDrawerLayout;
    CharSequence mTitle;
    int index = 0;
    ActionBarDrawerToggle  mDrawerToggle;
    FragmentTransaction fTrans;
    TopTrackFragment frag;
    TopTagFragment frag1;
    AlbumFragment frag2;
    FavoriteListFragment frag4;
    Dialog d;
    String longClickedArtist;
    int longClickedN;

    String[] sidePanel = new String[]{"Top Artist Tracks","Top Tag Tracks", "Artist Albums","Favorite Tracks"};//,"Last Played Tracks","Last Played Artist"};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        loadPref();
        int ret = getVKLoginInfo();
        LoadViews2();
        if(ret == 1)lastPlayList();


    }


    @Override
    protected void onStop(){
       super.onStop();
    }

    void loadPref()
    {
        SharedPreferences settings = getSharedPreferences("MadPlayerPreferences", 0);

        accessToken  = settings.getString("accessToken", null);
        expiresIn  = settings.getLong("expiresIn", -1);
        userId  = settings.getString("userId", null);
        loginIP = settings.getString("loginIP", null);
        loginTime = settings.getLong("loginTime", -1);

    }
    void lastPlayList()
    {

        //frag4 = new FavoriteListFragment();
         setFavoriteFragmentOnTop();
//        setArtistAlbumsFragmentOnTop();
//        setTagTopTracksFragmentOnTop();
        setArtistTopTracksFragmentOnTop();

    }
    void setArtistTopTracksFragmentOnTop()
    {
        fTrans = getFragmentManager().beginTransaction();

        if(frag == null)
        {
            frag = new TopTrackFragment();
            fTrans.add(R.id.linear,frag);
            SetLongClicked st = new SetLongClicked();
            st.execute(0);
        }
        else
        {
            fTrans.show(frag);
        }
        if(frag1 !=null)
            fTrans.hide(frag1);
        if(frag2 != null)
        {
            fTrans.hide(frag2);
            if(frag2.frag3 != null)
                fTrans.hide(frag2.frag3);
        }
        if(frag4 != null)
            fTrans.hide(frag4);
        fTrans.commit();

        if(frag1 != null)
            if(frag1.controls != null)
                frag1.controls.setPause();
        if(frag4 != null)
            if(frag4.controls != null)
                frag4.controls.setPause();
        if(frag2 != null)
        {
            if(frag2.controls != null)
                frag2.controls.setPause();
            if(frag2.frag3 != null)
                if(frag2.frag3.controls != null)
                frag2.frag3.controls.setPause();
        }

        if(frag.artist != null)
        {
            artist = frag.artist;
            artist = artist.replaceAll("%20", " ");
            mTitle = "Top "+artist + " Tracks";
        }
    }
    void setTagTopTracksFragmentOnTop()
    {
        fTrans = getFragmentManager().beginTransaction();
        if(frag1 == null)
        {
            frag1 = new TopTagFragment();
            fTrans.add(R.id.linear,frag1);
            SetLongClicked st = new SetLongClicked();
            st.execute(1);
        }
        else
        {
            fTrans.show(frag1);
            frag1.setPlayList();
        }
        if(frag != null)
            fTrans.hide(frag);
        if(frag4 != null)
            fTrans.hide(frag4);
        if(frag2 != null)
        {
            fTrans.hide(frag2);
            if(frag2.frag3 != null)
                fTrans.hide(frag2.frag3);
        }
        fTrans.commit();
        if(frag != null)
            if(frag.controls != null)
                frag.controls.setPause();
        if(frag4 != null)
            if(frag4.controls != null)
                frag4.controls.setPause();
        if(frag2 != null)
        {
            if(frag2.controls != null)
                frag2.controls.setPause();
            if(frag2.frag3 != null)
                if(frag2.frag3.controls != null)
                    frag2.frag3.controls.setPause();
        }

        if(frag1.tag != null)
        {
            tag = frag1.tag;
            tag = tag.replaceAll("%20", " ");
            mTitle = "Top "+tag + " Tracks";
        }
    }
    void setArtistAlbumsFragmentOnTop()
    {
        fTrans = getFragmentManager().beginTransaction();
        if(frag2 == null)
        {
            frag2 = new AlbumFragment();
            fTrans.add(R.id.linear,frag2);
            SetLongClicked st = new SetLongClicked();
            st.execute(2);

        }
        else
        {
            fTrans.show(frag2);
        }
        if(frag != null)
            fTrans.hide(frag);
        if(frag1 != null)
            fTrans.hide(frag1);
        if(frag4 != null)
            fTrans.hide(frag4);
        if(frag2 != null)
            if(frag2.frag3 != null)
                fTrans.hide(frag2.frag3);
        fTrans.commit();
        if(frag != null)
            if(frag.controls != null)
                frag.controls.setPause();
        if(frag1 != null)
                if(frag1.controls != null)
                    frag1.controls.setPause();
        if(frag2 != null)
            if(frag2.frag3 != null)
                if(frag2.frag3.controls != null)
                    frag2.frag3.controls.setPause();

        if(frag2.artist != null)
        {
            artist = frag2.artist;
            artist = artist.replaceAll("%20", " ");
            mTitle = "Albums "+artist;
        }
    }
    void setFavoriteFragmentOnTop()
    {
        fTrans = getFragmentManager().beginTransaction();
        if(frag4 == null)
        {
            frag4 = new FavoriteListFragment();
            fTrans.add(R.id.linear, frag4);
            SetLongClicked st = new SetLongClicked();
            st.execute(3);

        }
        else
        {
            fTrans.show(frag4);
        }


        if(frag != null)
            fTrans.hide(frag);
        if(frag1 !=null)
            fTrans.hide(frag1);
        if(frag2 != null)
        {
            fTrans.hide(frag2);
            if(frag2.frag3 != null)
                fTrans.hide(frag2.frag3);
        }

        fTrans.commit();


        if(frag != null)
            if(frag.controls != null)
                frag.controls.setPause();
        if(frag1 != null)
            if(frag1.controls != null)
                frag1.controls.setPause();
        if(frag2 != null)
        {
            if(frag2.frag3 != null)
                if(frag2.frag3.controls != null)
                    frag2.frag3.controls.setPause();
        }
    }

    OnItemClickListener sideItemClick = new OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            mTitle = sidePanel[i];

            if(i == 0)
            {
                searchView.setQueryHint("Enter artist name");
                setArtistTopTracksFragmentOnTop();
            }
            else if(i  == 1)
            {
                setTagTopTracksFragmentOnTop();
                searchView.setQueryHint("Enter Tag");
            }
            else if(i  == 2)
            {
                setArtistAlbumsFragmentOnTop();
                searchView.setQueryHint("Enter artist name");
            }
            else if(i == 3)
            {
                setFavoriteFragmentOnTop();
            }
            mDrawerList.setItemChecked(i, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            index = i;
        }
    };
	
	
	boolean isInternetConnection()
	{
		boolean isConnect;
		
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        isConnect = networkInfo != null && networkInfo.isConnected();
		
		return isConnect;		
	}
	
	int getVKLoginInfo()
	{
		
		Calendar c = Calendar.getInstance(); 
		long currentTime = c.getTimeInMillis();
		
		if (isInternetConnection())
        {
			String ipAddress = null;
		    try
            {
		        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
                {
		            NetworkInterface inf = en.nextElement();
		            for (Enumeration<InetAddress> enumIp = inf.getInetAddresses(); enumIp.hasMoreElements();)
                    {
		                InetAddress inetAddress = enumIp.nextElement();
		                if (!inetAddress.isLoopbackAddress())
                        {
		                    ipAddress = inetAddress.getHostAddress();
		                }
		            }
		        }
		    } catch (SocketException ignored) {}
			
			if(accessToken == null || !ipAddress.contentEquals(loginIP) || (currentTime/1000 - loginTime/1000) > expiresIn)
			{
				Intent intent = new Intent(this, WebActivity.class);
				startActivityForResult(intent, 9);
                return 0;
			}
            else
            {
                return 1;
            }
			
	    }
        else
        {
	    	Toast.makeText(this, "Not Internet connect", Toast.LENGTH_LONG).show();		    	
	    	this.finish();
	    }
        return 0;
	}
	
	@Override 
	public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
        searchView.setOnQueryTextListener(searchKeyListener);
        searchView.setIconifiedByDefault(false);
        searchMenuItem = menu.findItem(R.id.search_button);

        return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if(frag2 != null && index == 2)
            {
                if(frag2.frag3 != null && !frag2.frag3.isHidden())
                {
                    fTrans = getFragmentManager().beginTransaction();
                    fTrans.show(frag2);
                    fTrans.hide(frag2.frag3);
                    fTrans.commit();
                    frag2.frag3.controls.setPause();
                }
                else
                    moveTaskToBack(true);
            }
            else
                moveTaskToBack(true);
			return false;
		}

		return super.onKeyDown(keyCode, event);
	}
	
	OnQueryTextListener searchKeyListener = new OnQueryTextListener()
    {
		@Override
		public boolean onQueryTextChange(String newText)
        {
			return false;
		}

		@Override
		public boolean onQueryTextSubmit(String query)
        {
			if(index == 0)//bar.getSelectedNavigationIndex() == 0)
			{
				artist = query;
                mTitle = "Top "+artist + " Tracks";
				artist = artist.replaceAll(" ", "%20");
                getActionBar().setTitle(mTitle);
                frag.searchArtistTopTrack(artist);
			}
			else if(index == 1)//bar.getSelectedNavigationIndex() == 1)
			{
				tag = query;
                mTitle = "Top "+tag + " Tracks";
				tag = tag.replaceAll(" ", "%20");
                getActionBar().setTitle(mTitle);
                frag1.searchTagTopTrack(tag);
			}
            else if(index == 2)
            {
                artist = query;
                mTitle = ""+artist + " Albums";
                artist = artist.replaceAll(" ", "%20");
                getActionBar().setTitle(mTitle);
                frag2.searchArtistAlbums(artist);
            }
			searchMenuItem.collapseActionView();

			return false;
		}
    };

    void LoadViews2()
    {
        bar = getActionBar();
        Drawable dr = getResources().getDrawable(R.drawable.ac_back);
        bar.setBackgroundDrawable(dr);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);


        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.sidebar_text  /*android.R.layout.simple_list_item_1*/, sidePanel));

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_launcher,  /* nav drawer image to replace 'Up' caret */
                android.R.drawable.arrow_down_float,  /* "open drawer" description for accessibility */
                android.R.drawable.arrow_up_float  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mTitle);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerList.setOnItemClickListener(sideItemClick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 9)
        {
            if (resultCode == RESULT_OK)
            {
                accessToken = data.getStringExtra("accessToken");
                expiresIn = data.getLongExtra("expiresIn", -1);
                userId = data.getStringExtra("userId");
                loginIP = data.getStringExtra("loginIP");
                loginTime = data.getLongExtra("loginTime", -1);
                lastPlayList();
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (mDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        // Handle action buttons
        switch(item.getItemId())
        {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    AdapterView.OnItemLongClickListener trackLongClickListener = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            TextView tv = (TextView)view.findViewById(R.id.artistListTextView);
            longClickedArtist = tv.getText().toString();
            longClickedN = i;
            d = new Dialog(view.getContext());
            d.setContentView(R.layout.test_dialog);
            d.setTitle("Choose your path...");
            ListView lv = (ListView)d.findViewById(R.id.menuList);
            String[] menu = new String[0];
            if(index == 0 || index == 1 || index == 2)
            {
                menu = new String[]{"Download","Get artist albums","Get artist top tracks","Add to favorite"};
            }
            if(index == 3)
            {
                menu = new String[]{"Download","Get artist albums","Get artist top tracks","Delete to favorite"};
            }
            lv.setAdapter(new ArrayAdapter<String>(d.getContext(), android.R.layout.simple_list_item_1,menu));
            lv.setOnItemClickListener(menuClickListener);

            d.show();

            return false;
        }
    };
    AdapterView.OnItemClickListener menuClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3)
        {
            TextView tv = (TextView)v;


            if(tv.getText().toString().compareTo("Download") == 0)
            {
                MadPlayer.Track track;

                if(index == 0)
                {
                    track = frag.player.playList.get(longClickedN);
                    frag.player.getTrackInfo(track,track.n,0);
                    DownloadTrack dt = new DownloadTrack();
                    dt.execute(track.n,0);
                }
                if(index == 1)
                {
                    track = frag1.player.playList.get(longClickedN);
                    frag1.player.getTrackInfo(track,track.n,0);
                    DownloadTrack dt = new DownloadTrack();
                    dt.execute(track.n,1);
                }
                if(index == 2)
                {
                    track = frag2.frag3.player.playList.get(longClickedN);
                    frag2.frag3.player.getTrackInfo(track,track.n,0);
                    DownloadTrack dt = new DownloadTrack();
                    dt.execute(track.n,2);
                }
                if(index == 3)
                {
                    track = frag4.player.playList.get(longClickedN);
                    frag4.player.getTrackInfo(track,track.n,0);
                    DownloadTrack dt = new DownloadTrack();
                    dt.execute(track.n,3);
                }

                Toast.makeText(v.getContext(), "Download", Toast.LENGTH_LONG).show();
            }
            if(tv.getText().toString().compareTo("Get artist albums") == 0)
            {
                Toast.makeText(v.getContext(), "Get "+longClickedArtist+" albums", Toast.LENGTH_LONG).show();

                setArtistAlbumsFragmentOnTop();
                frag2.searchArtistAlbums(longClickedArtist);
            }
            if(tv.getText().toString().compareTo("Get artist top tracks") == 0)
            {
                setArtistTopTracksFragmentOnTop();
                frag.searchArtistTopTrack(longClickedArtist);
                Toast.makeText(v.getContext(), "Get "+longClickedArtist+" top tracks", Toast.LENGTH_LONG).show();
            }
            if(tv.getText().toString().compareTo("Add to favorite") == 0)
            {
                Toast.makeText(v.getContext(), "Add to favorite", Toast.LENGTH_LONG).show();
                MadPlayer.Track track;
                if(index == 0)
                {
                    track = frag.player.playList.get(longClickedN);
                    frag4.addToFavorite(track);
                }
                if(index == 1)
                {
                    track = frag1.player.playList.get(longClickedN);
                    frag4.addToFavorite(track);
                }
                if(index == 2)
                {
                    track = frag2.frag3.player.playList.get(longClickedN);
                    frag4.addToFavorite(track);
                }
            }
            if(tv.getText().toString().compareTo("Delete to favorite") == 0)
            {
                frag4.deleteToFavorite(longClickedN);
            }
            d.cancel();
        }
    };

    class SetLongClicked extends AsyncTask<Integer, Void, Void>
    {

        @Override
        protected Void doInBackground(Integer... i)
        {
            if(i[0] == 0)
            {
                int ret = 0;
                while (ret == 0)
                {
                    if(frag.controls != null)
                        ret = frag.controls.setLongClicked(trackLongClickListener);
                }
            }

            if(i[0] == 1)
            {
                int ret = 0;
                while (ret == 0)
                {
                    if(frag1.controls != null)
                        ret = frag1.controls.setLongClicked(trackLongClickListener);
                }
            }

            if(i[0] == 2)
            {
                int ret = 1;
                while (ret == 0)
                {
                    if(frag2.controls != null)
                        ret = frag2.controls.setLongClicked(trackLongClickListener);
                }
            }
            if(i[0] == 3)
            {
                int ret = 0;
                while (ret == 0)
                {
                    if(frag4.controls != null)
                        ret = frag4.controls.setLongClicked(trackLongClickListener);
                }
            }
            return null;
        }
    }
    class DownloadTrack extends AsyncTask<Integer, Void, Void>
    {

        @Override
        protected Void doInBackground(Integer... i)
        {
            int ret = 0;
            MadPlayer.Track track = null;
            while (ret == 0)
            {
                if(i[1] == 0)
                    track = frag.player.playList.get(i[0]);
                if(i[1] == 1)
                    track = frag1.player.playList.get(i[0]);
                if(i[1] == 2)
                    track = frag2.frag3.player.playList.get(i[0]);
                if(i[1] == 3)
                    track = frag4.player.playList.get(i[0]);

                if(track.url != null || track.url.compareTo("") != 0)
                {
                    ret = 1;
                }
            }
            if(i[1] == 0)
                track = frag.player.playList.get(i[0]);
            if(i[1] == 1)
                track = frag1.player.playList.get(i[0]);
            if(i[1] == 2)
                track = frag2.frag3.player.playList.get(i[0]);
            if(i[1] == 3)
                track = frag4.player.playList.get(i[0]);

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(
                    Uri.parse(track.url));
            request.setTitle(track.artist+" - "+track.track);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS+"/maddoxPlayer/",track.artist+" - "+track.track+".mp3");
            dm.enqueue(request);
            return null;
        }
    }

}




