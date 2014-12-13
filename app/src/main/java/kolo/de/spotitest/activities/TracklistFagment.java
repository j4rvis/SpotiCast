package kolo.de.spotitest.activities;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import kolo.de.spotitest.R;
import kolo.de.spotitest.music_models.Track;
import kolo.de.spotitest.music_models.TrackAdapter;
import kolo.de.spotitest.user.User;


public class TracklistFagment extends Fragment {

    private OnTrackSelectedListenerListener mListener;
    private View mMainView;
    private User mUser;
    private ListView mListViewTracks;
    private TrackAdapter mAdapter;

    public TracklistFagment() {
        // Required empty public constructor
    }

    public TracklistFagment(User xCurrentUser) {
        this.mUser = xCurrentUser;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_tracklist, container, false);
        mListViewTracks = (ListView)mMainView.findViewById(R.id.listViewTracks);
        return mMainView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnTrackSelectedListenerListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void fillPlaylistList(final ArrayList<Track> list) {
        mAdapter = new TrackAdapter(getActivity(), list);
        mListViewTracks.setAdapter(mAdapter);

        mListViewTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onTrackSelected(list.get(position));
            }
        });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnTrackSelectedListenerListener {
        // TODO: Update argument type and name
        public void onTrackSelected(Track xSelectedTrack);
    }
}
