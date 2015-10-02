package com.edaviessmith.sms_roulette.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.edaviessmith.sms_roulette.Var;

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
    private String email;

    public Contact() {
        info = new ArrayList<>();
    }

    public Contact(Cursor cur) {
        info = new ArrayList<>();

        setId(cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID)));
        setDisplayName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

        String tUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
        String pUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

        if (Var.validateURI(tUri)) setThumbUri(Uri.parse(tUri));
        if (Var.validateURI(pUri)) setPhotoUri(Uri.parse(pUri));

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

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
