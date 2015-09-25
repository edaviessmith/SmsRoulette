package com.edaviessmith.sms_roulette.data;

import com.edaviessmith.sms_roulette.App;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Conversation {

    private int id;
    private Contact contact;
    private String number;

    private List<SMSData> smsDataList;

    public Conversation() {
        smsDataList = new ArrayList<>();
    }

    /**
     *
     * @param smsData
     * @return
     */
    public boolean addSmsData(SMSData smsData) {

        //TODO maybe use a more efficient way to check for duplicates
        for(SMSData msg: smsDataList) {
            if(smsData.getId() == msg.getId()) return false;
        }

        smsDataList.add(smsData);
        return true;
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

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = App.simplePhone(number);
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
