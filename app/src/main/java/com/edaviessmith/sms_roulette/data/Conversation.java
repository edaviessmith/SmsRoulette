package com.edaviessmith.sms_roulette.data;

import com.edaviessmith.sms_roulette.Var;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Conversation {

    private int id;
    private Contact contact;
    private String number;             // Used for organising conversations

    private List<String> rawNumbers;   // Used for querying sms messages

    private HashMap<Long, SmsData> smsDataList;

    public Conversation() {
        smsDataList = new HashMap<>();
        rawNumbers = new ArrayList<>();
    }

    private long newestMessage, oldestMessage;      //Keys to getting messages real time


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
    public boolean addSmsData(SmsData smsData) {

        if (smsDataList.containsKey(smsData.getDate())) return false;

        if (newestMessage == 0L || smsData.getDate() > newestMessage)
            newestMessage = smsData.getDate();
        if (oldestMessage == 0L || smsData.getDate() < oldestMessage)
            oldestMessage = smsData.getDate();

        smsDataList.put(smsData.getDate(), smsData);
        return true;
    }

    public void addSmsData(List<SmsData> smsDataList) {
        for (SmsData smsData : smsDataList) {
            addSmsData(smsData);
        }
    }


    public ArrayList<SmsData> sortedSmsData() {
        ArrayList<Long> dates = new ArrayList<>(smsDataList.keySet());
        Collections.sort(dates);
        ArrayList<SmsData> items = new ArrayList<>();
        for (Long date : dates) {
            for (Long key : smsDataList.keySet()) {
                if (date.equals(key)) {
                    items.add(smsDataList.get(key));
                }
            }
        }
        return items;
    }

    public HashMap<Long, SmsData> getSmsDataList() {
        return smsDataList;
    }


    public void setSmsDataList(HashMap<Long, SmsData> smsDataList) {
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
        this.number = Var.simplePhone(number);
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

    public long getNewestMessage() {
        return newestMessage;
    }

    public long getOldestMessage() {
        return oldestMessage;
    }


}
