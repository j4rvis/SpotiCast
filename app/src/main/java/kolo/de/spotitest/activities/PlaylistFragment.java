package kolo.de.spotitest.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import kolo.de.spotitest.R;
import kolo.de.spotitest.music_models.Playlist;
import kolo.de.spotitest.music_models.PlaylistAdapter;
import kolo.de.spotitest.user.User;


public class PlaylistFragment extends Fragment {

    private OnPlaylistSelectedListener mListener;
    private View mMainView;
    private ListView mListViewPlaylists;
    private User mUser;

    public PlaylistFragment() {
        // Required empty public constructor
    }

    public PlaylistFragment(User xCurrentUser) {
        this.mUser = xCurrentUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_playlist, container, false);
        return mMainView;
    }

    @Override
    public void onAttach(Activity xActivity) {
        super.onAttach(xActivity);
        try {
            mListener = (OnPlaylistSelectedListener) xActivity;
        } catch (ClassCastException e) {
            throw new ClassCastException(xActivity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void fillPlaylistList(final ArrayList<Playlist> xPlaylistList){
        mListViewPlaylists = (ListView)mMainView.findViewById(R.id.listViewPlaylists);
        PlaylistAdapter _Adapter = new PlaylistAdapter(getActivity(), xPlaylistList);
        mListViewPlaylists.setAdapter(_Adapter);

        mListViewPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener != null) {
                    mListener.onPlaylistSelected(xPlaylistList.get(position));
                }
            }
        });
    }

    public interface OnPlaylistSelectedListener {
        public void onPlaylistSelected(Playlist xSelectedPlaylist);
    }
}
