package spoticast.music_models;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Parcel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import spoticast.interfaces.RequestReceveied;
import spoticast.interfaces.TrackListFilled;
import spoticast.request.RequestTask;
import spoticast.user.User;

/**
 * Created by Patrick on 26.10.2014.
 */
public class Playlist implements RequestReceveied{

    private String mName;
    private String mID;
    private User mUser;
    private Context mContext;

    private ArrayList<Track> mTrackList;

    private final static String TRACKS_ENDPOINT = "https://api.spotify.com/v1/users/{user_id}/playlists/{playlist_id}";
    private ProgressDialog mDialog;

    public Playlist(String id, String name, User user){
        this.mID = id;
        this.mName = name;
        this.mUser = user;
    }

    public Playlist(Parcel in){
        String[] data = new String[2];

        in.readStringArray(data);
        this.mName = data[0];
        this.mID = data[1];
    }

    public void getTracks(Context context){
        this.mContext = context;

        String _FinalURL = TRACKS_ENDPOINT.replace("{user_id}", mUser.getID());
        _FinalURL = _FinalURL.replace("{playlist_id}",mID);

        initProgessDialog(context);

        new RequestTask(this, mUser.getAccessToken(), User.RequestType.TRACK).execute(_FinalURL);
    }

    public String getName() {
        return mName;
    }

    public String getID() {
        return mID;
    }

    private void initProgessDialog(Context context){
        mDialog = new ProgressDialog(context);
        mDialog.setTitle("Tracks holen");
        mDialog.show();
    }

    @Override
    public void OnUserDatatReveived(String xResult) {}

    @Override
    public void OnPlaylistReveived(String xResult) {}

    @Override
    public void OnTrackReveived(String xResult) {
        mDialog.cancel();
        mTrackList = new ArrayList<Track>();

        if(xResult != null){
            try {
                JSONArray _Tracks = new JSONObject(xResult).getJSONObject("tracks").getJSONArray("items");

                for (int i = 0; i < _Tracks.length(); i++){
                    mTrackList.add(new Track(_Tracks.getJSONObject(i).getJSONObject("track")));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        ((TrackListFilled)mContext).OnTrackListFilled(mTrackList);
    }
}
