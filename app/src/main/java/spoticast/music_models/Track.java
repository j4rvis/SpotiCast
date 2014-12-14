package spoticast.music_models;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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

        try {
            this.mName = trackObject.getString("name");
            this.mURI = trackObject.getString("uri");
            this.mDuration = trackObject.getLong("duration_ms");
            getArtistsFromJSON(trackObject);
            getAlbumDetailsFromJSON(trackObject);
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
                if(_AlbumImages.getJSONObject(i).getString("width").equals("300")){
                    new BitmapFromUrl().execute(_AlbumImages.getJSONObject(_Counter).getString("url"));
                }
            }
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

    public Bitmap getAlbumcover() {
        return mAlbumcover;
    }

    public String getAlbumname() {
        return mAlbumname;
    }

    private class BitmapFromUrl extends AsyncTask<String, Void, Bitmap>{

        @Override
        protected Bitmap doInBackground(String... params) {
            return getBitmapFromURL(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                mAlbumcover = bitmap;
            }
        }

        private Bitmap getBitmapFromURL(final String src) {
            Bitmap _ReturnBitmap = null;

                try {
                    URL url = new URL(src);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    _ReturnBitmap = BitmapFactory.decodeStream(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            return _ReturnBitmap;
        }
    }
}
