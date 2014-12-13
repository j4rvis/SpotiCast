package kolo.de.spotitest.interfaces;

/**
 * Created by Patrick on 25.10.2014.
 */
public interface RequestReceveied {
    public void OnUserDatatReveived(String xResult);
    public void OnPlaylistReveived(String xResult);
    public void OnTrackReveived(String xResult);
}
