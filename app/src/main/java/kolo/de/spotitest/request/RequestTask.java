package kolo.de.spotitest.request;

import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import kolo.de.spotitest.music_models.Playlist;
import kolo.de.spotitest.user.User;

/**
 * Created by Patrick on 25.10.2014.
 */
public class RequestTask extends AsyncTask<String, String, String> {

    private Context mContext;
    private String mHeader;
    private User mUser;
    private Playlist mPlaylist;
    private User.RequestType mRequestType;

    public RequestTask(Context context){
        this(context, null);
    }

    public RequestTask(Context context, String header){
        this.mContext = context;
        this.mHeader = header;
    }

    public RequestTask(User user, String header, User.RequestType requestType){
        this.mUser = user;
        this.mHeader = header;
        this.mRequestType = requestType;
    }

    public RequestTask(Playlist playlist, String header, User.RequestType requestType) {
        this.mPlaylist = playlist;
        this.mHeader = header;
        this.mRequestType = requestType;
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet _HttpGet = new HttpGet(uri[0]);

        if(mHeader != null)
            _HttpGet.setHeader("Authorization", "Bearer " + mHeader);

        HttpResponse response;
        String responseString = null;

        try {
            response = httpclient.execute(_HttpGet);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            out.close();
            responseString = out.toString();
        } catch (ClientProtocolException e) {
            //TODO Handle problems..
        } catch (IOException e) {
            //TODO Handle problems..
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(mRequestType == User.RequestType.USERDATA) {
            if(mUser != null)
                mUser.OnUserDatatReveived(result);
        }
        else if(mRequestType == User.RequestType.PLAYLIST){
            if(mUser != null)
                mUser.OnPlaylistReveived(result);
        }
        else if(mRequestType == User.RequestType.TRACK){}{
            if(mPlaylist != null)
                mPlaylist.OnTrackReveived(result);
        }
    }
}
