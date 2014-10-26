package kolo.de.spotitest;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.spotify.sdk.android.Spotify;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.authentication.SpotifyAuthentication;
import com.spotify.sdk.android.playback.ConnectionStateCallback;

import java.util.ArrayList;


public class MainActivity extends Activity implements ConnectionStateCallback, PlaylistFilled {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "0bd07f103e564b0386a5ba1124a565ba";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "koloo.de://callback";
    private static final String CLIENT_SECRET = "a10509be8d7a413e94b6c53972345b35";

    private ListView mListViewPlaylists;

    public static User CurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SpotifyAuthentication.openAuthWindow(CLIENT_ID, "token", REDIRECT_URI,
                new String[]{"user-read-private", "streaming", "playlist-read-private"}, null, this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri uri = intent.getData();
        if (uri != null) {
            AuthenticationResponse response = SpotifyAuthentication.parseOauthResponse(uri);
            CurrentUser = new User(this, response.getAccessToken());
        }
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
        mListViewPlaylists = (ListView)findViewById(R.id.listViewPlaylists);
        PlaylistAdapter _Adapter = new PlaylistAdapter(this, list);
        mListViewPlaylists.setAdapter(_Adapter);

        mListViewPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent _Intent = new Intent(getApplicationContext(), TracksActivity.class);
                _Intent.putExtra("selected_list", list.get(position));
                startActivity(_Intent);
            }
        });
    }
}