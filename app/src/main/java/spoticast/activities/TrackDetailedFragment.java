package spoticast.activities;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import spoticast.music_models.Track;
import spoticast.spoticast.R;


public class TrackDetailedFragment extends Fragment implements SeekBar.OnSeekBarChangeListener {

    private View mMainView;
    private OnFragmentInteractionListener mListener;
    private Track mSelectedTrack;
    private SeekBar mSeekbarDuration;

    public TrackDetailedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_track_detailed, container, false);
        mSeekbarDuration = (SeekBar)mMainView.findViewById(R.id.seekBarDuration);
        mSeekbarDuration.setOnSeekBarChangeListener(this);
        return mMainView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setSelectedTrack(Track xSelectedTrack) {
        this.mSelectedTrack = xSelectedTrack;
        ImageView _ImageView = (ImageView)mMainView.findViewById(R.id.imageViewAlbumCover);

        if(xSelectedTrack.getAlbumcover() != null){
            _ImageView.setImageBitmap(xSelectedTrack.getAlbumcover());
        }

        ((MainActivity)getActivity()).mPlayer.play(mSelectedTrack.getURI());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.d("onProgressChanged", "##");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        Log.d("onStartTrackingTouch", "##");
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Log.d("onStopTrackingTouch", "##");
    }

    public void onPlaybackEvent(PlayerNotificationCallback.EventType eventType, final PlayerState playerState) {
        if(eventType == PlayerNotificationCallback.EventType.PLAY){
            //TODO: Seekbar progess richtig behandeln
            mSeekbarDuration.setMax(playerState.durationInMs);
            new CountDownTimer(playerState.durationInMs, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mSeekbarDuration.setProgress((int) (playerState.durationInMs - millisUntilFinished));
                    Log.d("onTick", playerState.durationInMs - millisUntilFinished + "");
                }

                @Override
                public void onFinish() {

                }
            }.start();
        }
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
