package com.edaviessmith.sms_roulette.data;

import com.edaviessmith.sms_roulette.App;

import java.util.Date;

/**
 * Created by Ethan on 23/09/2015.
 */
public class SMSData {
    private int id;
    private String number;
    private String body;
    private Date date;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setNumber(String number) {
        this.number = App.simplePhone(number);
    }

    public String getNumber() {
        return number;
    }

    public String getBody() {
        return body;
    }
}