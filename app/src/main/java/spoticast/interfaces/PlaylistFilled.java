package spoticast.interfaces;

import java.util.ArrayList;

import spoticast.music_models.Playlist;

/**
 * Created by Patrick on 26.10.2014.
 */
public interface PlaylistFilled {
    public void OnPlaylistFilled(ArrayList<Playlist> list);
}
