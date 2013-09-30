package maddox.music.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import maddox.music.player.MadPlayer.Album;
import java.util.ArrayList;
import com.loopj.android.image.SmartImageView;

/**
 * Created by maddox on 18.06.13.
 */
public class AlbumListAdapter extends ArrayAdapter {

    ArrayList<Album> list;
    Context context;
    int hide = 0;

    public AlbumListAdapter(Context c, int resource, ArrayList<Album> list)
    {
        super(c, resource, list);
        this.list = list;
        context = c;
    }



    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v  = view;
        if (v == null) {

            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.grid_album_item, null);
        }

        Album a = list.get(i);

        if (a != null)
        {
            //ImageView iv = (ImageView) v.findViewById(R.id.albumImageView);
            //SmartImageView iv = (SmartImageView) v.findViewById(R.id.albumImageView);
            TextView tv = (TextView) v.findViewById(R.id.albumNameTextView);

            //iv.setImageBitmap(a.image);
//            if(a.image != null)
//                iv.setImageDrawable(a.image);
//            else
            //iv.setImageUrl(a.url);
            tv.setText(a.name);
        }

        return v;
    }

}
