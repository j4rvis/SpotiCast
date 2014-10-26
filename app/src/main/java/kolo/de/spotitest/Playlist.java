package kolo.de.spotitest;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Patrick on 26.10.2014.
 */
public class Playlist implements RequestReceveied, Parcelable{

    private String mName;
    private String mID;
    private User mUser;
    private Context mContext;

    private ArrayList<Track> mTrackList;

    private final static String TRACKS_ENDPOINT = "https://api.spotify.com/v1/users/{user_id}/playlists/{playlist_id}";

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
        String _FinalURL;
        _FinalURL = TRACKS_ENDPOINT.replace("{user_id}", MainActivity.CurrentUser.getID());
        _FinalURL = _FinalURL.replace("{playlist_id}",mID);
        new RequestTask(this, MainActivity.CurrentUser.getAccessToken(), User.RequestType.TRACK).execute(_FinalURL);
    }

    public String getName() {
        return mName;
    }

    public String getID() {
        return mID;
    }

    @Override
    public void OnUserDatatReveived(String xResult) {}

    @Override
    public void OnPlaylistReveived(String xResult) {}

    @Override
    public void OnTrackReveived(String xResult) {

        if(xResult != null){
            try {
                mTrackList = new ArrayList<Track>();
                JSONArray _Tracks = new JSONObject(xResult).getJSONObject("tracks").getJSONArray("items");

                for (int i = 0; i < _Tracks.length(); i++){
                    mTrackList.add(new Track(_Tracks.getJSONObject(i).getJSONObject("track")));
                }

                ((TrackListFilled)mContext).OnTrackListFilled(mTrackList);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {this.mName, this.mID});
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Playlist createFromParcel(Parcel in) {
            return new Playlist(in);
        }

        public Playlist[] newArray(int size) {
            return new Playlist[size];
        }
    };
}
