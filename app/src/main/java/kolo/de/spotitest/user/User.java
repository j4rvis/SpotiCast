package kolo.de.spotitest.user;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kolo.de.spotitest.interfaces.PlaylistFilled;
import kolo.de.spotitest.interfaces.RequestReceveied;
import kolo.de.spotitest.request.RequestTask;
import kolo.de.spotitest.music_models.Playlist;

/**
 * Created by Patrick on 26.10.2014.
 */
public class User implements RequestReceveied {

    private Context mContext;
    private String mAccessToken;
    private String mUserData;
    private String mUserPlaylists;

    private final String ACCESSTOKEN = "accesstoken";

    private String mUsername;
    private String mUserID;
    private ArrayList<Playlist> mPlaylists;

    public static enum RequestType{USERDATA, PLAYLIST, TRACK};

    private ProgressDialog mDialog;

    SharedPreferences mPreferences;

    private static final String CURRENT_USER_ENDPOINT = "https://api.spotify.com/v1/me";
    private static final String USER_PLAYLISTS_ENDPOINT = "https://api.spotify.com/v1/users/{user_id}/playlists";

    public User(Context context){
        this.mContext = context;
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        mDialog = new ProgressDialog(context);
        mDialog.setCancelable(false);
    }

    public User(Context context, String accessToken){
        this(context);
        this.mAccessToken = accessToken;

        saveAccessToken();
    }

    private void saveAccessToken() {
        SharedPreferences.Editor _PreferenceEditor = mPreferences.edit();
        _PreferenceEditor.putString(ACCESSTOKEN, mAccessToken);
        _PreferenceEditor.commit();
    }

    public String getAccessToken(){
        return mPreferences.getString(ACCESSTOKEN, null);
    }

    public void getUserData() {
        mDialog.setTitle("Benutzerdaten holen");
        mDialog.show();
        new RequestTask(this, mAccessToken, RequestType.USERDATA).execute(CURRENT_USER_ENDPOINT);
    }

    private void getUserPlaylists() {
        mDialog.setTitle("Playlists holen");
        mDialog.show();
        String _FinalURL = USER_PLAYLISTS_ENDPOINT.replace("{user_id}", mUserID);
        new RequestTask(this, mAccessToken, RequestType.PLAYLIST).execute(_FinalURL);
    }

    @Override
    public void OnUserDatatReveived(String xResult) {
        mUserData = xResult;
        mDialog.cancel();

        if(xResult != null){
            try {
                JSONObject _Userdata = new JSONObject(xResult);
                mUsername = _Userdata.getString("display_name");
                mUserID = _Userdata.getString("id");

                getUserPlaylists();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnPlaylistReveived(String xResult) {
        mUserPlaylists = xResult;
        mDialog.cancel();

        if(mUserPlaylists != null){
            try {
                mPlaylists = new ArrayList<Playlist>();
                JSONArray _Playlists = new JSONObject(mUserPlaylists).getJSONArray("items");
                JSONObject _TmpJSONObject;

                for (int i = 0; i < _Playlists.length(); i++){
                    _TmpJSONObject = _Playlists.getJSONObject(i);

                    mPlaylists.add(new Playlist(_TmpJSONObject.getString("id"), _TmpJSONObject.getString("name"), this));
                }
                ((PlaylistFilled)mContext).OnPlaylistFilled(mPlaylists);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void OnTrackReveived(String xResult) {

    }

    public String getID(){
        return  this.mUserID;
    }
}
