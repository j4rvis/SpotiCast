package spoticast.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.Config;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import java.io.IOException;
import java.util.ArrayList;

import spoticast.spoticast.R;
import spoticast.interfaces.PlaylistFilled;
import spoticast.interfaces.TrackListFilled;
import spoticast.music_models.Playlist;
import spoticast.music_models.Track;
import spoticast.user.User;


public class MainActivity extends ActionBarActivity implements Player.InitializationObserver, ConnectionStateCallback ,PlaylistFilled, TrackListFilled, PlaylistFragment.OnPlaylistSelectedListener, TracklistFagment.OnTrackSelectedListenerListener, PlayerNotificationCallback {

    private static final String CLIENT_ID = "0bd07f103e564b0386a5ba1124a565ba";
    private static final String REDIRECT_URI = "koloo.de://callback";
    private static final String CLIENT_SECRET = "a10509be8d7a413e94b6c53972345b35";

    private static final String APP_ID = "52DB54C0";
    public static final String NAMESPACE = "urn:x-cast:com.ls.cast.sample";
    private static final int REQUEST_GMS_ERROR = 0;
    private static final String TAG = "TAG";

    private User mCurrentUser;
    private PlaylistFragment mPlaylistFragment;
    private TracklistFagment mTracklistFragment;
    private TrackDetailedFragment mTrackdetailedFragment;
    private Fragment mCurrentFragment;

    private final Cast.Listener castClientListener = new Cast.Listener(){
        @Override
        public void onApplicationDisconnected(int statusCode)
        {
            try
            {
                Cast.CastApi.removeMessageReceivedCallbacks(apiClient, NAMESPACE);
            }
            catch (IOException e)
            {
                Log.w(TAG, "Exception while launching application", e);
            }
            setSelectedDevice(null);
            setSessionStarted(false);
        }

        @Override
        public void onVolumeChanged()
        {
            if (apiClient != null)
            {
                Log.d(TAG, "onVolumeChanged: " + Cast.CastApi.getVolume(apiClient));
            }
        }
    };

    private final ResultCallback<Cast.ApplicationConnectionResult> connectionResultCallback = new ResultCallback<Cast.ApplicationConnectionResult>(){
        @Override
        public void onResult(Cast.ApplicationConnectionResult result){
            Status status = result.getStatus();
            if (status.isSuccess()){
                applicationStarted = true;

                try
                {
                    Cast.CastApi.setMessageReceivedCallbacks(apiClient, NAMESPACE, incomingMsgHandler);
                }
                catch (IOException e)
                {
                    Log.e(TAG, "Exception while creating channel", e);
                }

                setSessionStarted(true);
            }
            else{
                setSessionStarted(false);
            }
        }
    };

    private final GoogleApiClient.ConnectionCallbacks connectionCallback = new GoogleApiClient.ConnectionCallbacks(){
        @Override
        public void onConnected(Bundle bundle){
            try
            {
                Cast.CastApi.launchApplication(apiClient, APP_ID, false).setResultCallback(connectionResultCallback);
            }
            catch (Exception e)
            {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int i){
        }
    };

    private final GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener(){
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult)
        {
            setSelectedDevice(null);
            setSessionStarted(false);
        }
    };

    public final Cast.MessageReceivedCallback incomingMsgHandler = new Cast.MessageReceivedCallback(){

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace, String message){
            //Log.d(TAG, String.format("message namespace: %s message: %s", namespace, message));
        }
    };

    private final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback(){
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route)
        {
            Log.d(TAG, "onRouteSelected: " + route.getName());

            CastDevice device = CastDevice.getFromBundle(route.getExtras());
            setSelectedDevice(device);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route)
        {
            Log.d(TAG, "onRouteUnselected: " + route.getName());
            stopApplication();
            setSelectedDevice(null);
            setSessionStarted(false);
        }
    };


    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;

    private CastDevice selectedDevice;
    private GoogleApiClient apiClient;

    private boolean applicationStarted;
    public Player mPlayer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setActionbarText("");

        //TODO: Berechtigung ändern. playlist-read-public oder so
        SpotifyAuthentication.openAuthWindow(CLIENT_ID, "token", REDIRECT_URI,
                new String[]{"user-read-private", "streaming", "playlist-read-private", "playlist-modify-public"}, null, this);

        mPlaylistFragment = new PlaylistFragment(mCurrentUser);
        mTracklistFragment = new TracklistFagment(mCurrentUser);
        mTrackdetailedFragment = new TrackDetailedFragment();

        getFragmentManager().beginTransaction().add(R.id.fragment_container, mPlaylistFragment).commit();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, mTracklistFragment).commit();
        getFragmentManager().beginTransaction().add(R.id.fragment_container, mTrackdetailedFragment).commit();
        getFragmentManager().beginTransaction().hide(mTracklistFragment).commit();
        getFragmentManager().beginTransaction().hide(mTrackdetailedFragment).commit();

        this.setCurrentFragment(mPlaylistFragment);

        mediaRouter = MediaRouter.getInstance(getApplicationContext());
        mediaRouteSelector = new MediaRouteSelector.Builder().addControlCategory(CastMediaControlIntent.categoryForCast(APP_ID)).build();
    }

    private void setActionbarText(String xText) {
        ActionBar _Actionbar = getSupportActionBar();
        _Actionbar.setTitle(xText);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            mCurrentUser = new User(this, response.getAccessToken());
            mCurrentUser.getUserData();

            Config _PlayerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
            Spotify _SpotifyPlayer = new Spotify();
            mPlayer = _SpotifyPlayer.getPlayer(_PlayerConfig, this, this);
        }
    }

    @Override
    public void onBackPressed() {
        if(mCurrentFragment instanceof PlaylistFragment){
            super.onBackPressed();
        }
        else if(mCurrentFragment instanceof TracklistFagment){
            getFragmentManager().beginTransaction().hide(mTracklistFragment).commit();
            getFragmentManager().beginTransaction().show(mPlaylistFragment).commit();
            setCurrentFragment(mPlaylistFragment);
            setActionbarText("");
        }
        else if(mCurrentFragment instanceof TrackDetailedFragment){
            getFragmentManager().beginTransaction().hide(mTrackdetailedFragment).commit();
            getFragmentManager().beginTransaction().show(mTracklistFragment).commit();
            setCurrentFragment(mTracklistFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);

        return true;
    }

    @Override
    protected void onStart(){
        super.onStart();
        mediaRouter.addCallback(mediaRouteSelector, mediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    protected void onStop(){
        setSelectedDevice(null);
        mediaRouter.removeCallback(mediaRouterCallback);
        super.onStop();
    }

    @Override
    protected void onResume(){
        super.onResume();
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (errorCode != ConnectionResult.SUCCESS)
        {
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, REQUEST_GMS_ERROR).show();
        }
    }

    @Override
    protected void onPause(){
        disconnectApiClient();
        super.onPause();
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

        setActionbarText(xSelectedPlaylist.getName());
    }

    @Override
    public void onTrackSelected(Track xSelectedTrack) {
        sendMessage(xSelectedTrack.getName());

        getFragmentManager().beginTransaction().hide(mTracklistFragment).commit();
        getFragmentManager().beginTransaction().show(mTrackdetailedFragment).commit();
        mTrackdetailedFragment.setSelectedTrack(xSelectedTrack);
        setCurrentFragment(mTrackdetailedFragment);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Chromecast Funktionen

    private void sendMessage(String message){
        if (apiClient != null)
        {
            try
            {
                Cast.CastApi.sendMessage(apiClient, NAMESPACE, message)
                        .setResultCallback(new ResultCallback<Status>(){
                            @Override
                            public void onResult(Status result)
                            {
                                if (!result.isSuccess())
                                {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            }
            catch (Exception e)
            {
                Log.e(TAG, "Exception while sending message", e);
            }
        }
    }

    private void setSessionStarted(boolean enabled){

    }

    private void setSelectedDevice(CastDevice device)
    {
        Log.d(TAG, "setSelectedDevice: " + device);

        selectedDevice = device;

        if (selectedDevice != null)
        {
            try
            {
                stopApplication();
                disconnectApiClient();
                connectApiClient();
            }
            catch (IllegalStateException e)
            {
                Log.w(TAG, "Exception while connecting API client", e);
                disconnectApiClient();
            }
        }
        else
        {
            disconnectApiClient();
            mediaRouter.selectRoute(mediaRouter.getDefaultRoute());
        }
    }

    private void connectApiClient(){
        Cast.CastOptions apiOptions = Cast.CastOptions.builder(selectedDevice, castClientListener).build();
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptions)
                .addConnectionCallbacks(connectionCallback)
                .addOnConnectionFailedListener(connectionFailedListener)
                .build();
        apiClient.connect();
    }

    private void disconnectApiClient(){
        if (apiClient != null)
        {
            apiClient.disconnect();
            apiClient = null;
        }
    }

    private void stopApplication(){
        if (apiClient == null) return;

        if (applicationStarted)
        {
            Cast.CastApi.stopApplication(apiClient);
            applicationStarted = false;
        }
    }

    @Override
    public void onInitialized() {
        mPlayer.addConnectionStateCallback(this);
        mPlayer.addPlayerNotificationCallback(this);
    }

    @Override
    public void onError(Throwable throwable) {
        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.e("onPlaybackEvent", eventType.name());
        ((TrackDetailedFragment)getFragmentManager().findFragmentById(R.id.fragment_container)).onPlaybackEvent(eventType, playerState);
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {

    }

    public void playTrack(View view){
        Log.e("playTrack", "##");
        mPlayer.resume();
    }

    public void pauseTrack(View view){
        Log.e("pauseTrack", "##");
        mPlayer.pause();
    }
}