package kolo.de.spotitest.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.ConnectionStateCallback;

import java.util.ArrayList;

import kolo.de.spotitest.interfaces.TrackListFilled;
import kolo.de.spotitest.music_models.Playlist;
import kolo.de.spotitest.interfaces.PlaylistFilled;
import kolo.de.spotitest.R;
import kolo.de.spotitest.music_models.Track;
import kolo.de.spotitest.user.User;


public class MainActivity extends ActionBarActivity implements ConnectionStateCallback ,PlaylistFilled, TrackListFilled, PlaylistFragment.OnPlaylistSelectedListener, TracklistFagment.OnTrackSelectedListenerListener {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "0bd07f103e564b0386a5ba1124a565ba";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "koloo.de://callback";
    private static final String CLIENT_SECRET = "a10509be8d7a413e94b6c53972345b35";

    private User mCurrentUser;
    private PlaylistFragment mPlaylistFragment;
    private TracklistFagment mTracklistFragment;
    private Fragment mCurrentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TODO: Berechtigung ändern. playlist-read-public oder so
        SpotifyAuthentication.openAuthWindow(CLIENT_ID, "token", REDIRECT_URI,
                new String[]{"user-read-private", "streaming", "playlist-read-private"}, null, this);

        mPlaylistFragment = new PlaylistFragment(mCurrentUser);
        mTracklistFragment = new TracklistFagment(mCurrentUser);

        getFragmentManager().beginTransaction().add(R.id.fragment_container, mPlaylistFragment).commit();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, mTracklistFragment).commit();
        getFragmentManager().beginTransaction().hide(mTracklistFragment).commit();

        this.setCurrentFragment(mPlaylistFragment);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            mCurrentUser = new User(this, response.getAccessToken());
            mCurrentUser.getUserData();
        }
    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment instanceof PlaylistFragment){
            super.onBackPressed();
        }
        else{
            getFragmentManager().beginTransaction().hide(mTracklistFragment).commit();
            getFragmentManager().beginTransaction().show( mPlaylistFragment).commit();
        }
    }

    private void setCurrentFragment(Fragment xFragment){
        this.mCurrentFragment = xFragment;
    }

    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onNewCredentials(String s) {
        Log.d("MainActivity", "User credentials blob received");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    protected void onDestroy() {
        // VERY IMPORTANT! This must always be called or else you will leak resources
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    public void OnPlaylistFilled(final ArrayList<Playlist> list) {
        mPlaylistFragment.fillPlaylistList(list);
    }

    @Override
    public void OnTrackListFilled(ArrayList<Track> list) {
        mTracklistFragment.fillPlaylistList(list);
    }

    @Override
    public void onPlaylistSelected(Playlist xSelectedPlaylist) {

        getFragmentManager().beginTransaction().hide(mPlaylistFragment).commit();
        getFragmentManager().beginTransaction().show( mTracklistFragment).commit();
        setCurrentFragment(mTracklistFragment);

        xSelectedPlaylist.getTracks(this);
    }

    @Override
    public void onTrackSelected(Track xSelectedTrack) {

    }
}