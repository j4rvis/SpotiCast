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
import com.google.android.gms.common.api.GoogleApiClient;
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
    private static final String APPID = "CC2ECB12";

    private ListView mListViewTracks;
    private Spotify mSpotify;
    private Player mPlayer;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private MediaRouter.Callback mMediaRouterCallback;
    private CastDevice mSelectedDevice;
    private GoogleApiClient mApiClient;
    private Cast.Listener mCastListener;
    private GoogleApiClient.ConnectionCallbacks mConnectionCallbacks;
    private ConnectionFailedListener mConnectionFailedListener;
    private HelloWorldChannel mHelloWorldChannel;
    private boolean mApplicationStarted;
    private boolean mWaitingForReconnect;
    private String mSessionId;
    RemoteMediaPlayer mRemoteMediaPlayer = new RemoteMediaPlayer();
    private MediaRouter.Callback mCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        mListViewTracks = (ListView)findViewById(R.id.listViewTracks);
        Playlist _List = getIntent().getExtras().getParcelable("selected_list");
        _List.getTracks(this);

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        // Create a MediaRouteSelector for the type of routes your app supports
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
                .addControlCategory(CastMediaControlIntent.categoryForCast(APPID)).build();

        // Create a MediaRouter callback for discovery events
        mMediaRouterCallback = new MyMediaRouterCallback();

        mCastListener = new CastListener();

        mRemoteMediaPlayer = new RemoteMediaPlayer();
        mRemoteMediaPlayer
                .setOnStatusUpdatedListener(new RemoteMediaPlayer.OnStatusUpdatedListener() {
                    @Override
                    public void onStatusUpdated() {
                        MediaStatus mediaStatus = mRemoteMediaPlayer
                                .getMediaStatus();
                        boolean isPlaying = mediaStatus.getPlayerState() == MediaStatus.PLAYER_STATE_PLAYING;
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
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);

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
                        //mPlayer.play(_TrackURI);
                        Play();
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        });

    }

    private void Play() {
        try {
            if (mRemoteMediaPlayer != null) {
                mRemoteMediaPlayer.pause(mApiClient);
                Log.e("mRemoteMediaPlayer", "isNull");
            } else {
                Log.e("MainActivity", "Play");
                MediaMetadata mediaMetadata = new MediaMetadata(
                        MediaMetadata.MEDIA_TYPE_MOVIE);
                mediaMetadata.putString(MediaMetadata.KEY_TITLE, "My video");
                MediaInfo mediaInfo = new MediaInfo.Builder(
                        "http://www.youtube.com/watch?feature=player_embedded&v=uK4_x_qq_E8")
                        .setContentType("video/mp4")
                        .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                        .setMetadata(mediaMetadata).build();
                mRemoteMediaPlayer
                        .load(mApiClient, mediaInfo, true)
                        .setResultCallback(
                                new ResultCallback<RemoteMediaPlayer.MediaChannelResult>() {

                                    @Override
                                    public void onResult(
                                            RemoteMediaPlayer.MediaChannelResult result) {
                                        if (result.getStatus().isSuccess()) {
                                        }
                                    }
                                });
            }
        } catch (IllegalStateException e) {
            Log.e("IllegalStateException", String.valueOf(e));
        } catch (Exception e) {
            Log.e("Exception", String.valueOf(e));
        }
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
        mPlayer.resume();
    }

    private final MediaRouter.Callback mediaRouterCallback = new MediaRouter.Callback()
    {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route)
        {
            CastDevice device = CastDevice.getFromBundle(route.getExtras());
            //setSelectedDevice(device);
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route)
        {
            //setSelectedDevice(null);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();

        // Add the callback to start device discovery
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onPause() {
        // Remove the callback to stop device discovery
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onPause();
    }


    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            // Handle route selection.
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());

            // Just display a message for now; In a real app this would be the
            // hook  to connect to the selected device and launch the receiver
            // app
            Log.d(TAG, "onRouteSelected");
            launchReceiver();

        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            Log.d(TAG, "onRouteUnselected: info=" + info);
            teardown();
            mSelectedDevice = null;
        }
    }

    /**
     * Start the receiver app
     */
    private void launchReceiver() {
        try {
            mCastListener = new Cast.Listener() {

                @Override
                public void onApplicationDisconnected(int errorCode) {
                    Log.d(TAG, "application has stopped");
                    teardown();
                }

            };
            // Connect to Google Play services
            mConnectionCallbacks = new ConnectionCallbacks();
            mConnectionFailedListener = new ConnectionFailedListener();
            Cast.CastOptions.Builder apiOptionsBuilder = Cast.CastOptions
                    .builder(mSelectedDevice, mCastListener);
            mApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Cast.API, apiOptionsBuilder.build())
                    .addConnectionCallbacks(mConnectionCallbacks)
                    .addOnConnectionFailedListener(mConnectionFailedListener)
                    .build();

            mApiClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "Failed launchReceiver", e);
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionCallbacks implements
            GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle connectionHint) {
            Log.d(TAG, "onConnected");

            if (mApiClient == null) {
                // We got disconnected while this runnable was pending
                // execution.
                return;
            }

            try {
                if (mWaitingForReconnect) {
                    mWaitingForReconnect = false;

                    // Check if the receiver app is still running
                    if ((connectionHint != null)
                            && connectionHint
                            .getBoolean(Cast.EXTRA_APP_NO_LONGER_RUNNING)) {
                        Log.d(TAG, "App  is no longer running");
                        teardown();
                    } else {
                        // Re-create the custom message channel
                        try {
                            Cast.CastApi.setMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace(),
                                    mHelloWorldChannel);
                        } catch (IOException e) {
                            Log.e(TAG, "Exception while creating channel", e);
                        }
                    }
                } else {
                    // Launch the receiver app
                    Cast.CastApi
                            .launchApplication(mApiClient, APPID, false)
                            .setResultCallback(
                                    new ResultCallback<Cast.ApplicationConnectionResult>() {
                                        @Override
                                        public void onResult(
                                                Cast.ApplicationConnectionResult result) {
                                            Status status = result.getStatus();
                                            Log.d(TAG,
                                                    "ApplicationConnectionResultCallback.onResult: statusCode"
                                                            + status.getStatusCode());
                                            if (status.isSuccess()) {
                                                ApplicationMetadata applicationMetadata = result
                                                        .getApplicationMetadata();
                                                mSessionId = result
                                                        .getSessionId();
                                                String applicationStatus = result
                                                        .getApplicationStatus();
                                                boolean wasLaunched = result
                                                        .getWasLaunched();
                                                Log.d(TAG,
                                                        "application name: "
                                                                + applicationMetadata
                                                                .getName()
                                                                + ", status: "
                                                                + applicationStatus
                                                                + ", sessionId: "
                                                                + mSessionId
                                                                + ", wasLaunched: "
                                                                + wasLaunched);
                                                mApplicationStarted = true;

                                                // Create the custom message
                                                // channel
                                                mHelloWorldChannel = new HelloWorldChannel();
                                                try {
                                                    Cast.CastApi
                                                            .setMessageReceivedCallbacks(
                                                                    mApiClient,
                                                                    mHelloWorldChannel
                                                                            .getNamespace(),
                                                                    mHelloWorldChannel);
                                                } catch (IOException e) {
                                                    Log.e(TAG,
                                                            "Exception while creating channel",
                                                            e);
                                                }

                                                // set the initial instructions
                                                // on the receiver
                                                sendMessage("Senden");
                                            } else {
                                                Log.e(TAG,
                                                        "application could not launch");
                                                teardown();
                                            }
                                        }
                                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to launch application", e);
            }
        }

        @Override
        public void onConnectionSuspended(int cause) {
            Log.d(TAG, "onConnectionSuspended");
            mWaitingForReconnect = true;
        }
    }

    /**
     * Google Play services callbacks
     */
    private class ConnectionFailedListener implements
            GoogleApiClient.OnConnectionFailedListener {
        @Override
        public void onConnectionFailed(ConnectionResult result) {
            Log.e(TAG, "onConnectionFailed ");

            teardown();
        }
    }

    /**
     * Tear down the connection to the receiver
     */
    private void teardown() {
        Log.d(TAG, "teardown");
        if (mApiClient != null) {
            if (mApplicationStarted) {
                if (mApiClient.isConnected()  || mApiClient.isConnecting()) {
                    try {
                        Cast.CastApi.stopApplication(mApiClient, mSessionId);
                        if (mHelloWorldChannel != null) {
                            Cast.CastApi.removeMessageReceivedCallbacks(
                                    mApiClient,
                                    mHelloWorldChannel.getNamespace());
                            mHelloWorldChannel = null;
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception while removing channel", e);
                    }
                    mApiClient.disconnect();
                }
                mApplicationStarted = false;
            }
            mApiClient = null;
        }
        mSelectedDevice = null;
        mWaitingForReconnect = false;
        mSessionId = null;
    }

    /**
     * Send a text message to the receiver
     *
     * @param message
     */
    private void sendMessage(String message) {
        if (mApiClient != null && mHelloWorldChannel != null) {
            try {

                Log.e(TAG, "Sending message: " + message);
                Play();
                Cast.CastApi.sendMessage(mApiClient,
                        mHelloWorldChannel.getNamespace(), message)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status result) {
                                if (!result.isSuccess()) {
                                    Log.e(TAG, "Sending message failed");
                                }
                            }
                        });
            } catch (Exception e) {
                Log.e(TAG, "Exception while sending message", e);
            }
        } else {
            Toast.makeText(this, message, Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * Custom message channel
     */
    class HelloWorldChannel implements Cast.MessageReceivedCallback {

        /**
         * @return custom namespace
         */
        public String getNamespace() {
            return "urn:x-cast:Namespace";
        }

        /*
         * Receive message from the receiver app
         */
        @Override
        public void onMessageReceived(CastDevice castDevice, String namespace,
                                      String message) {
            Log.d(TAG, "onMessageReceived: " + message);
        }
    }

    private class CastListener extends Cast.Listener {

        @Override
        public void onApplicationStatusChanged() {
//			Toast.makeText(CastActivity.this, "testing", Toast.LENGTH_SHORT).show();
            if (mApiClient != null) {
                Log.e("",
                        "onApplicationStatusChanged: "
                                + Cast.CastApi.getApplicationStatus(mApiClient));
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
}
