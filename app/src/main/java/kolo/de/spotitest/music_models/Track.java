package kolo.de.spotitest.music_models;

import android.graphics.Bitmap;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Patrick on 26.10.2014.
 */
public class Track {

    private String mName;
    private String mArtists;
    private String mURI;
    private Bitmap mCover;
    private long mDuration;

    public Track(JSONObject trackObject){
        try {
            this.mName = trackObject.getString("name");
            this.mURI = trackObject.getString("uri");
            this.mDuration = trackObject.getLong("duration_ms");
        } catch (JSONException e) {}
    }

    public String getName(){
        return this.mName;
    }

    public String getURI(){
        return this.mURI;
    }
}
