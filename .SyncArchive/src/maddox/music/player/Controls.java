package maddox.music.player;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * Created by maddox on 30.06.13.
 */
public class Controls
{
    TextView playedText;
    ImageButton backwardButton, forwardButton;
    ToggleButton randomButton, playButton, reparedButton;
    SeekBar seekBar;
    ListView songView;
    MadPlayer player;
    PlayListAdapter adapter;
    Dialog d;
    String longClickedArtist;

    View LoadControls(View v)
    {
        player = null;
        adapter = null;
        //Buttons
        backwardButton = (ImageButton)v.findViewById(R.id.ImageBackwardButton);
        forwardButton = (ImageButton)v.findViewById(R.id.ImageForwardButton);
        randomButton = (ToggleButton)v.findViewById(R.id.ToggleShuffleButton);
        playButton = (ToggleButton)v.findViewById(R.id.TogglePlayButton);
        reparedButton = (ToggleButton)v.findViewById(R.id.reparedButton);
        randomButton.setOnCheckedChangeListener(randomCheckListener);
        playButton.setOnClickListener(playCheckListener);
        forwardButton.setOnClickListener(forwardClickListener);
        backwardButton.setOnClickListener(backwardClickListener);
        reparedButton.setOnCheckedChangeListener(reparedCheckListener);

        //List views
        songView = (ListView)v.findViewById(R.id.SongView);
        songView.setOnItemClickListener(trackClickListener);
        //songView.setOnItemLongClickListener(trackLongClickListener);


        //Text views
        playedText = (TextView)v.findViewById(R.id.trackTextView);
        playedText.setSelected(true);

        //SeekBars
        seekBar = (SeekBar)v.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        //seekBar.setThumb(tum);
        //seekBar.getThumb().mutate().setAlpha(0);
        return v;
    }

    void setPA(MadPlayer p, PlayListAdapter a)
    {
        if(player == null)
            player = p;
        if(adapter == null)
            adapter = a;
    }
    int setLongClicked(AdapterView.OnItemLongClickListener lis)
    {
        if(songView !=null)
        {
            songView.setOnItemLongClickListener(lis);
            return 1;
        }
        else
        {
            return 0;
        }

    }

    CompoundButton.OnCheckedChangeListener randomCheckListener = new CompoundButton.OnCheckedChangeListener()
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if(player != null)
                if(isChecked)
                    player.randomPlaying = 1;
                else
                    player.randomPlaying = 0;
        }
    };
    View.OnClickListener playCheckListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if(player != null)
                if(player.playList.size() > 0)
                {
                    int  n = player.getCurrentTrack();
                    if(n < 0) n = 0;
                    player.PlayTrack(n);
                    if(player.getPlayerStat() == 2)
                        playButton.setChecked(false);
                    else playButton.setChecked(true);
                }
        }
    };
    void setPause()
    {
        if(player != null)
            if(player.playList.size() > 0)
            {
                if(player.playerStat == 1)
                {
                    int  n = player.getCurrentTrack();
                    if(n < 0) n = 0;
                    player.PlayTrack(n);
                    if(player.getPlayerStat() == 2)
                        playButton.setChecked(false);
                    else playButton.setChecked(true);
                }
            }
    }
    View.OnClickListener forwardClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if(player != null)
                player.PlayNextTrack(1);

            playButton.setChecked(true);
            adapter.setDuration(0, 0, 0);
        }
    };
    View.OnClickListener backwardClickListener = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            if(player != null)
                player.PlayNextTrack(2);

            playButton.setChecked(true);
            adapter.setDuration(0, 0, 0);
        }
    };
    CompoundButton.OnCheckedChangeListener reparedCheckListener = new CompoundButton.OnCheckedChangeListener()
    {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
        {
            if(player != null)
                if(isChecked)
                    player.repared = 1;
                else
                    player.repared = 0;
        }
    };
    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener()
    {
        @Override
        public void onProgressChanged(SeekBar seekBar1, int i, boolean b)
        {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar1)
        {
            //seekBar.getThumb().mutate().setAlpha(100);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar1)
        {
            player.setSeek(seekBar.getProgress());
            Log.d("MP", "" + seekBar.getProgress());
            //seekBar.getThumb().mutate().setAlpha(0);
        }
    };
    AdapterView.OnItemClickListener trackClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3)
        {
            adapter.setDuration(0, 0, 0);
            player.PlayTrack(arg2);

            if(player.getPlayerStat() == 2)
            {
                playButton.setChecked(false);
            }
            else
            {
                playButton.setChecked(true);
            }
        }
    };
    AdapterView.OnItemLongClickListener trackLongClickListener = new AdapterView.OnItemLongClickListener(){
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l)
        {
            TextView tv = (TextView)view.findViewById(R.id.artistListTextView);
            longClickedArtist = tv.getText().toString();
            d = new Dialog(view.getContext());
            d.setContentView(R.layout.test_dialog);
            d.setTitle("Choose your path...");
            ListView lv = (ListView)d.findViewById(R.id.menuList);
            String[] menu= new String[]{"Download","Get artist albums","Get artist top tracks","Add to favorite"};
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
                Toast.makeText(v.getContext(), "Download", Toast.LENGTH_LONG).show();
            }
            if(tv.getText().toString().compareTo("Get artist albums") == 0)
            {
                Toast.makeText(v.getContext(), "Get "+longClickedArtist+" albums", Toast.LENGTH_LONG).show();
            }
            if(tv.getText().toString().compareTo("Get artist top tracks") == 0)
            {
                Toast.makeText(v.getContext(), "Get "+longClickedArtist+" top tracks", Toast.LENGTH_LONG).show();
            }
            if(tv.getText().toString().compareTo("Add to favorite") == 0)
            {
                Toast.makeText(v.getContext(), "Add to favorite", Toast.LENGTH_LONG).show();
            }
            d.cancel();
        }
    };

}
