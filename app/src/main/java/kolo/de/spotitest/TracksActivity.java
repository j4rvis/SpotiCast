package kolo.de.spotitest;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.cast.CastDevice;
import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.playback.ConnectionStateCallback;
import com.spotify.sdk.android.playback.Player;
import com.spotify.sdk.android.playback.PlayerNotificationCallback;
import com.spotify.sdk.android.playback.PlayerState;

import java.util.ArrayList;


public class TracksActivity extends Activity implements TrackListFilled, PlayerNotificationCallback, ConnectionStateCallback {

    private ListView mListViewTracks;
    private Spotify mSpotify;
    private Player mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        mListViewTracks = (ListView)findViewById(R.id.listViewTracks);
        Playlist _List = getIntent().getExtras().getParcelable("selected_list");
        _List.getTracks(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tracks, menu);

        /*MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mediaRouteSelector);*/

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

    private MediaRouter mediaRouter;
    private MediaRouteSelector mediaRouteSelector;
}
