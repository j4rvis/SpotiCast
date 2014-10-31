package kolo.de.spotitest;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaControlIntent;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.RemoteMediaPlayer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import java.io.IOException;
import java.util.ArrayList;


public class TracksActivity extends ActionBarActivity implements TrackListFilled, PlayerNotificationCallback, ConnectionStateCallback {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String APP_ID = "381E888C";

    private ListView mListViewTracks;
    private Spotify mSpotify;
    private Player mPlayer;

    private MediaRouter mRouter;
    private MediaRouter.Callback mCallback;
    private MediaRouteSelector mSelector;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastListener;
    private ConnectionCallbacks mConnectionCallbacks;
    boolean mWaitingForReconnect = false;
    private ConnectionFailedListener mConnectionFailedListener;
    HelloWorldChannel mHelloWorldChannel = new HelloWorldChannel();
    RemoteMediaPlayer mRemoteMediaPlayer = new RemoteMediaPlayer();
    Button play;
    Button play2;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        mListViewTracks = (ListView)findViewById(R.id.listViewTracks);
        Playlist _List = getIntent().getExtras().getParcelable("selected_list");
        _List.getTracks(this);

        mRouter = MediaRouter.getInstance(getApplicationContext());
        mSelector = new MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
                .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                .build();
        mCallback = new MyCallback();
        mCastListener = new CastListener();
        mConnectionCallbacks = new ConnectionCallbacks();
        mConnectionFailedListener = new ConnectionFailedListener();

        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer
                .setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
                    @Override
                    public void onStatusUpdated() {
                        MediaStatus mediaStatus;
                        MediaInfo mediaInfo;
                        if(mRemoteMediaPlayer!=null){
                            mediaStatus = mRemoteMediaPlayer.getMediaStatus();
                            mediaInfo = mRemoteMediaPlayer.getMediaInfo();
                            if(mediaStatus!=null){
                                //you could even check if mediaStatus.getPlayerState() is not null here
                                //before the next line of code
                                boolean RemoteisPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
                            }
                        }
                    }
                });

        mRemoteMediaPlayer
                .setOnMetadataUpdatedListener(new RemoteMediaPlayer.OnMetadataUpdatedListener() {
                    @Override
                    public void onMetadataUpdated() {
                        MediaInfo mediaInfo = mRemoteMediaPlayer.getMediaInfo();
                        MediaMetadata metadata = mediaInfo.getMetadata();
                    }
                });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_tracks, menu);

        /*MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_control_frame);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);*/

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat
                .getActionProvider(mediaRouteMenuItem);
        // Set the MediaRouteActionProvider selector for device discovery.
        mediaRouteActionProvider.setRouteSelector(mSelector);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnTrackListFilled(final ArrayList<Track> list) {
        TrackAdapter _Adapter = new TrackAdapter(this, list);
        mListViewTracks.setAdapter(_Adapter);

        mListViewTracks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String _TrackURI = list.get(position).getURI();
                mSpotify = new Spotify(MainActivity.CurrentUser.getAccessToken());
                mPlayer = mSpotify.getPlayer(getApplicationContext(), "My Company Name", this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized() {
                        mPlayer.addConnectionStateCallback(TracksActivity.this);
                        mPlayer.addPlayerNotificationCallback(TracksActivity.this);
                        mPlayer.play(_TrackURI);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        });

    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {

    }

    @Override
    public void onLoggedIn() {

    }

    @Override
    public void onLoggedOut() {

    }

    @Override
    public void onLoginFailed(Throwable throwable) {

    }

    @Override
    public void onTemporaryError() {

    }

    @Override
    public void onNewCredentials(String s) {

    }

    @Override
    public void onConnectionMessage(String s) {

    }

    public void stopPlaying(View view) {
        mPlayer.pause();
    }

    public void resumePlaying(View view) {
       // mPlayer.resume();
        //Play();
    }

    // Add the callback on start to tell the media router what kinds of routes
    // the application is interested in so that it can try to discover suitable
    // ones.
    public void onStart() {
        super.onStart();

        mRouter.addCallback(mSelector, mCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);

        MediaRouter.RouteInfo route = mRouter.updateSelectedRoute(mSelector);
        // do something with the route...
    }

    // Remove the selector on stop to tell the media router that it no longer
    // needs to invest effort trying to discover routes of these kinds for now.
    public void onStop() {
        //setSelectedDevice(null);
        mRouter.removeCallback(mCallback);
        super.onStop();
    }

    private final class MyCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);
            CastDevice device = CastDevice.getFromBundle(route.getExtras());
            setSelectedDevice(device);

            String routeId = route.getId();
            Toast.makeText(getApplicationContext(), "RoutedId " + routeId, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);
            mSelectedDevice = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS) {
            GooglePlayServicesUtil.getErrorDialog(errorCode, this, 0).show();
        }
    }

    private void setSelectedDevice(CastDevice device) {
        Log.d("setSelectedDevice 1", "setSelectedDevice: " + device);
        mSelectedDevice = device;

        if (mSelectedDevice != null) {
            Log.e("Testing  ", "selcted" + mSelectedDevice);
            try {
                disconnectApiClient();
                connectApiClient();

                Cast.CastOptions.builder(mSelectedDevice,
                        mCastListener)
                        .setVerboseLoggingEnabled(true)
                        .build();
            } catch (IllegalStateException e) {
                Log.w("", "Exception while connecting API client", e);
                disconnectApiClient();
            }
        } else {
            if (mApiClient != null) {
                if (mApiClient.isConnected()) {
                }
                disconnectApiClient();
            }
            mRouter.selectRoute(mRouter.getDefaultRoute());
        }
    }

    private void connectApiClient() {
        Log.e("Connection checking", " Inside Connect Status Before");
        Cast.CastOptions apiOptions = Cast.CastOptions.builder(mSelectedDevice, mCastListener).build();

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Cast.API, apiOptions)
                .addConnectionCallbacks(mConnectionCallbacks)
                .addOnConnectionFailedListener(mConnectionFailedListener)
                .build();

        mApiClient.connect();
        Log.e("Connection checking", mApiClient.isConnected() + "Status");
    }

    private void disconnectApiClient() {
        if (mApiClient != null) {
            mApiClient.disconnect();
            mApiClient = null;
        }
    }

    private class CastListener extends Cast.Listener {

        @Override
        public void onApplicationStatusChanged() {
            if (mApiClient != null) {
                Log.e("","onApplicationStatusChanged: "+ Cast.CastApi.getApplicationStatus(mApiClient));
                Toast.makeText(getApplicationContext(), Cast.CastApi.getApplicationStatus(mApiClient), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onVolumeChanged() {
            if (mApiClient != null) {
                Log.e("",
                        "onVolumeChanged: "
                                + Cast.CastApi.getVolume(mApiClient));
            }
        }

        @Override
        public void onApplicationDisconnected(int statusCode) {
            Log.e("", "Cast.Listener.onApplicationDisconnected: " + statusCode);
            try {
                Cast.CastApi.removeMessageReceivedCallbacks(mApiClient,
                        mRemoteMediaPlayer.getNamespace());
            } catch (IOException e) {
                Log.w("", "Exception while launching application", e);
            }
        }
    }

    private class ConnectionFailedListener implements GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e("Testing 8", "onConnectionFailed  Connection Call back");
            setSelectedDevice(null);
        }
    }

    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {

        @Override
        public void onConnected(Bundle connectionHint) {
            Log.e("Testing 2", "onConnected Connection Call back");
            Cast.CastApi.launchApplication(mApiClient, APP_ID)
                    .setResultCallback(new ConnectionResultCallback());
            //Play();
            //StartLink("https://www.google.de");
            sendMessage("Hi");
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.e("Testing 2", "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }

    private final class ConnectionResultCallback implements ResultCallback<Cast.ApplicationConnectionResult> {
        @Override
        public void onResult(Cast.ApplicationConnectionResult result) {
            Status status = result.getStatus();
            ApplicationMetadata appMetaData = result.getApplicationMetadata();

            if (status.isSuccess()) {
                Log.e("Testing 3", "ConnectionResultCallback");
                ApplicationMetadata applicationMetadata = result.getApplicationMetadata();
                String sessionId = result.getSessionId();
                String applicationStatus = result.getApplicationStatus();
                boolean wasLaunched = result.getWasLaunched();

                try {
                    Cast.CastApi.setMessageReceivedCallbacks(mApiClient,
                            mRemoteMediaPlayer.getNamespace(),
                            mRemoteMediaPlayer);
                    Play();
                    mRemoteMediaPlayer.requestStatus(mApiClient).setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                        @Override
                        public void onResult(RemoteMediaPlayer.MediaChannelResult mediaChannelResult) {
                            Log.e("MediaStatus", mediaChannelResult.getStatus().toString());
                        }
                    });
                    Log.e("Connecting", "mRemoteMediaPlayer");
                } catch (IOException e) {
                    Log.e("Testing Exception", "ConnectionResultCallback");
                }
            }
        }
    }

    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {
                Cast.CastApi.sendMessage(mApiClient,
                        mHelloWorldChannel.getNamespace(), message)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                }
                            }
                        });
            } catch (Exception e) {
            }
        }
    }

    private void Play() {
        try {
            PendingResult<RemoteMediaPlayer.MediaChannelResult> _Status = mRemoteMediaPlayer.requestStatus(mApiClient);
            if(!_Status.isCanceled()) {
                MediaMetadata mediaMetadata = new MediaMetadata(
                        MediaMetadata.MEDIA_TYPE_MOVIE);
                mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");
                MediaInfo mediaInfo = new MediaInfo.Builder(
                        "https://www.youtube.com/watch?v=ZLA2S5wQO2E")
                        .setContentType("video/mp4")
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata).build();

                mRemoteMediaPlayer.load(mApiClient, mediaInfo, true)
                        .setResultCallback(new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {
                            @Override
                            public void onResult(RemoteMediaPlayer.MediaChannelResult result) {
                                Log.d(TAG, result.getStatus().toString());
                                if (result.getStatus().isSuccess()) {
                                    Log.d(TAG, "Media loaded successfully");
                                } else {
                                    Log.d(TAG, result.getStatus().toString());
                                }
                            }
                        });
            }


        } catch (IllegalStateException e) {
            Log.v("IllegalStateException", String.valueOf(e));
        } catch (Exception e) {
            Log.v("Exception", String.valueOf(e));
        }
    }

    private void startPlay(){
        mRemoteMediaPlayer.play(mApiClient);
    }

    private void StartLink(String link) {
        mHelloWorldChannel.sendMessage(mApiClient, link);
    }

    public class HelloWorldChannel implements Cast.MessageReceivedCallback {

        private static final String NAMESPACE = "urn:x-cast:kolo.de.spotitest";

        public String getNamespace() {
            return NAMESPACE;
        }

        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                                      String message) {
            Log.d("", "onMessageReceived: " + message);
        }

        public void sendMessage(GoogleApiClient apiClient, String message) {
            Cast.CastApi.sendMessage(apiClient, NAMESPACE, message).setResultCallback(
                    new SendMessageResultCallback(message));
        }

        private class SendMessageResultCallback implements ResultCallback<Status> {
            String mMessage;

            SendMessageResultCallback(String message) {
                mMessage = message;
            }

            @Override
            public void onResult(Status result) {
                if (!result.isSuccess()) {
                    Log.d("", "Failed to send message. statusCode: " + result.getStatusCode()
                            + " message: " + mMessage);
                }
            }
        }
    }
}
