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
    private String number;             // Used for organising conversations

    private List<String> rawNumbers;   // Used for querying sms messages

    private List<SMSData> smsDataList;

    public Conversation() {
        smsDataList = new ArrayList<>();
        rawNumbers = new ArrayList<>();
    }



    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     *
     * @param smsData
     * @return true if the sms data was added
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

    public List<String> getRawNumbers() {
        return rawNumbers;
    }

    public void setRawNumbers(List<String> rawNumbers) {
        this.rawNumbers = rawNumbers;
    }

    public void addRawNumber(String number) {
        if(!rawNumbers.contains(number)) rawNumbers.add(number);
    }
}
