package com.edaviessmith.sms_roulette.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Conversation {

    private int id;
    private Contact contact;
    private String phone;

    private List<SMSData> smsDataList;

    public Conversation() {
        smsDataList = new ArrayList<>();
    }

    public List<SMSData> getSmsDataList() {
        return smsDataList;
    }

    public void setSmsDataList(List<SMSData> smsDataList) {
        this.smsDataList = smsDataList;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
