package maddox.music.player;

import java.util.List;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PlayListAdapter extends ArrayAdapter {
	
	List<String> items;
	int pos;
	int pl = 0;
	int currentDuration = 0;
	float duration = 0;
	int isPlay = 0;
	float buffProgress;
	
	public PlayListAdapter(Context context, int resource, List<String> items, int pos) {

	    super(context, resource, items);
	    this.items = items;
	    this.pos = pos;
        //this.lv = lv;
	}
	
	public void setPos(int pos)
	{
		if(this.pos != pos)
			pl = 0;
		this.pos = pos;
		
	}
	public int getPos()
	{
		return pos;
	}
	public void setDuration(int currentDuration, int duration, int buffProgress)
	{
		this.currentDuration = currentDuration;
		this.duration = duration;
		this.buffProgress = buffProgress;
		this.notifyDataSetChanged();
	}
	public void setPlay(int  n)
	{
		isPlay = n;
		this.notifyDataSetChanged();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;

	    if (v == null) {

	        LayoutInflater vi;
	        vi = LayoutInflater.from(getContext());
	        v = vi.inflate(R.layout.text, null);
	    }


	    String p = items.get(position);
	    String[] separated = p.split(":");

	    if (p != null) {

	        TextView tt = (TextView) v.findViewById(R.id.text);
	        TextView att = (TextView)v.findViewById(R.id.artistListTextView);
	        ImageView iv = (ImageView) v.findViewById(R.id.imagePlayView);
	        ProgressBar pb = (ProgressBar) v.findViewById(R.id.progressDurationBar);
	        TextView pt = (TextView) v.findViewById(R.id.durationView);
	        TextView rpt = (TextView) v.findViewById(R.id.reversDurationView);
	        

	        if (tt != null) {
	            tt.setText(separated[1].replaceAll("%20"," "));
	            att.setText(separated[0].replaceAll("%20"," "));
	        }
	        
	        if(position == pos)
	        {
                //lv.smoothScrollToPosition(pos);
	        	if(iv != null)
	        	{
	        		if(iv.getVisibility() == View.VISIBLE)
	        		{
	        			if(isPlay == 2)
	        			{
	        				iv.setImageResource(android.R.drawable.ic_media_pause);
	        				//pl = 1;
	        			}
	        			else if(isPlay == 1)
	        			{
	        				iv.setImageResource(android.R.drawable.ic_media_play);
	        				//pl = 0;
	        			}
	        		}
	        		else
	        		{
	        			iv.setImageResource(android.R.drawable.ic_media_play);
	        			//pl = 0;
	        		}

	        		iv.setVisibility(View.VISIBLE);
	        	}
	        	
	        	if(pb != null)
	        	{
	        		pb.setMax((int)duration);	        		
	        		pb.setProgress(currentDuration/1000);
	        		float secondary  = (duration/100) * buffProgress;
	        		pb.setSecondaryProgress((int)secondary);
	        		pb.setVisibility(View.VISIBLE);
	        	}

	        	if(pt != null)
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
	    			
	    			pt.setText(mstr+""+sstr);
	    			pt.setVisibility(View.VISIBLE);
	        	}
	        	
	        	if(rpt != null)
	        	{
	        		int min, sek;
	        		
	        		min = ((int)duration - (currentDuration/1000))/60;
	        		sek = ((int)duration - (currentDuration/1000))- min*60;
	        		
	        		String mstr = "-"+min+":";
	    			String sstr = "";
	    			
	    			if(sek < 10)
	    				sstr = "0"+sek;
	    			else
	    				sstr += sek;
	        		
	    			rpt.setText(mstr+""+sstr);
	        		rpt.setVisibility(View.VISIBLE);
	        	}

	        }
	        else
	        {
	        	if(iv != null)
	        	{
	        		iv.setVisibility(View.INVISIBLE);
	        	}
	        	if(pb != null)
	        	{
	        		pb.setVisibility(View.INVISIBLE);
	        	}
	        	if(pt != null)
	        	{
	        		pt.setVisibility(View.INVISIBLE);
	        	}
	        	if(rpt != null)
	        	{
	        		rpt.setVisibility(View.INVISIBLE);
	        	}
	        }
	        
	    }

	    return v;		
		
	
	}

}
