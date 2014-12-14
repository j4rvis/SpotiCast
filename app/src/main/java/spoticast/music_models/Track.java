package spoticast.music_models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Patrick on 26.10.2014.
 */
public class Track {

    private String mName;
    private String mArtists = "";
    private String mURI;
    private Bitmap mCover;
    private long mDuration;
    private String mAlbumname;
    private Bitmap mAlbumcover;

    public Track(JSONObject trackObject){

        //https://play.spotify.com/user/1157211646/playlist/2tgQdTqZ1mSReLWjznc2cL?play=true&utm_source=open.spotify.com&utm_medium=open
        try {
            this.mName = trackObject.getString("name");
            this.mURI = trackObject.getString("uri");
            this.mDuration = trackObject.getLong("duration_ms");
            getArtistsFromJSON(trackObject);
        } catch (JSONException e) {}
    }

    private void getArtistsFromJSON(JSONObject trackObject){
        try {
            JSONArray _ArtistsArray = trackObject.getJSONArray("artists");

            for (int i = 0; i < _ArtistsArray.length(); i++){

                if(i == 0){
                    this.mArtists += _ArtistsArray.getJSONObject(i).getString("name");
                }
                else{
                    this.mArtists += ", " + _ArtistsArray.getJSONObject(i).getString("name");
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void getAlbumDetailsFromJSON(JSONObject trackObject){

        try {
            JSONObject _AlbumObject = trackObject.getJSONObject("album");
            this.mAlbumname = _AlbumObject.getString("name");

            final JSONArray _AlbumImages = _AlbumObject.getJSONArray("images");
            for (int i = 0; i < _AlbumImages.length(); i++){
                final int _Counter = i;
                if(_AlbumImages.getJSONObject(i).getString("width").equals("64")){
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mAlbumcover = getBitmapFromURL(_AlbumImages.getJSONObject(_Counter).getString("url"));
                                Log.e("Artist", mAlbumcover.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }.run();
                }
            }

            Log.e("Artist", this.mAlbumname);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getName(){
        return this.mName;
    }

    public String getURI(){
        return this.mURI;
    }

    public String getArtists(){
        return this.mArtists;
    }

    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            return null;
        }
    }
}
