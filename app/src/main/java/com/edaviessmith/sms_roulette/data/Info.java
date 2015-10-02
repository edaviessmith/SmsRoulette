package com.edaviessmith.sms_roulette.data;

import android.database.Cursor;
import android.provider.ContactsContract;

import com.edaviessmith.sms_roulette.Var;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Info {

    private Var.Category category; // Possibly an enum (phone, email, event)
    private int type;  // Work, Home, Mobile (sub category)
    private String value; // May change to object for future extensions

    public Info() {
    }

    public Info(Var.Category category) {
        this.category = category;
    }

    public Info(Var.Category category, int type, String value) {
        this.category = category;
        this.type = type;
        this.value = value;
    }

    public Info(Var.Category cat, Cursor pCur) {
        setCategory(cat);
        if (category == Var.Category.PHONE) {
            setType(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
            setValue(Var.simplePhone(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
        }
        if (category == Var.Category.EMAIL) {
            setType(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE)));
            setValue(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
        }
    }

    public Var.Category getCategory() {
        return category;
    }

    public void setCategory(Var.Category category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
