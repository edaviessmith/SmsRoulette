package com.edaviessmith.sms_roulette.data;

import android.database.Cursor;

import com.edaviessmith.sms_roulette.Var;

/**
 * Created by Ethan on 23/09/2015.
 */
public class SmsData {
    private int id;
    private String number;
    private String rawNumber;
    private String body;
    private long date;
    private Var.MsgType type;

    public SmsData() {
    }

    public SmsData(Cursor c) {
        setId(c.getInt(c.getColumnIndexOrThrow("_id")));
        setBody(c.getString(c.getColumnIndexOrThrow("body")));
        setType(Var.getMsgType(c.getString(c.getColumnIndexOrThrow("type"))));
        setDate(c.getLong(c.getColumnIndexOrThrow("date")));
        setNumber(c.getString(c.getColumnIndexOrThrow("address")));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public long getDate() {
        return date;
    }

    public void setNumber(String number) {
        this.number = Var.simplePhone(number);
        setRawNumber(number);
    }

    public String getNumber() {
        return number;
    }

    public String getBody() {
        return body;
    }

    public void setType(Var.MsgType type) {
        this.type = type;
    }

    public Var.MsgType getType() {
        return type;
    }

    public void setRawNumber(String rawNumber) {
        this.rawNumber = rawNumber;
    }

    public String getRawNumber() {
        return rawNumber;
    }
}
