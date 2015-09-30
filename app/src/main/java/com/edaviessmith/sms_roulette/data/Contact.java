package com.edaviessmith.sms_roulette.data;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Contact {

    private long id;
    private String displayName;

    private List<Info> info;
    private Uri thumbUri;
    private Uri photoUri;

    public Contact() {
        info = new ArrayList<>();
    }

    public List<Info> getInfo() {
        return info;
    }

    public void setInfo(List<Info> info) {
        this.info = info;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setThumbUri(Uri thumbUri) {
        this.thumbUri = thumbUri;
    }

    public Uri getThumbUri() {
        return thumbUri;
    }

    public void setPhotoUri(Uri photoUri) {
        this.photoUri = photoUri;
    }

    public Uri getPhotoUri() {
        return photoUri;
    }

    public long getId() {
        return id;
    }
}
