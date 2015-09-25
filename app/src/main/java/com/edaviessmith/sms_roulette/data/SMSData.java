package com.edaviessmith.sms_roulette.data;

import java.util.Date;

/**
 * Created by Ethan on 23/09/2015.
 */
public class SMSData {
    private String number;
    private String body;
    private Date date;

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
        this.number = number;
    }

    public String getNumber() {
        return number;
    }

    public String getBody() {
        return body;
    }
}
