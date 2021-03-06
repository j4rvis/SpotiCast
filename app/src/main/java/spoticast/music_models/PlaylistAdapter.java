package spoticast.music_models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import spoticast.spoticast.R;

/**
 * Created by Patrick on 26.10.2014.
 */
public class PlaylistAdapter extends BaseAdapter{

    private ArrayList<Playlist> mList;
    private Context mContext;

    public  PlaylistAdapter(Context context, ArrayList<Playlist> list){
        this.mContext = context;
        this.mList = list;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater _Inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View _RowView = _Inflater.inflate(R.layout.playlist_item, parent, false);

        TextView _PlaylistName = (TextView) _RowView.findViewById(R.id.textViewPlaylistName);
        _PlaylistName.setText(mList.get(position).getName());
        return _RowView;
    }
}
