package com.edaviessmith.sms_roulette;

import com.edaviessmith.sms_roulette.data.SmsData;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Var {

    public enum Category {EMAIL, PHONE}

    public enum MsgType {RECEIVED, SENT, DRAFT, OTHER}

    public enum Feed {IDLE, PENDING, DONE}

    public static int LIMIT = 20;
    public static int LIST_OFFSET = 20;


    public static String getTimeSince(long publishedDate) {
        String date = "";

        if (publishedDate <= 0) {
            //date = context.getResources().getString(R.string.loading_date);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(publishedDate / 1000);

            Calendar now = Calendar.getInstance();
            SimpleDateFormat s;
            if (cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                s = new SimpleDateFormat("h:mm a", Locale.getDefault());
            } else {
                if (cal.get(Calendar.YEAR) != now.get(Calendar.YEAR)) {
                    s = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                } else {
                    s = new SimpleDateFormat("MMM d", Locale.getDefault());
                }

            }
            date += s.format(publishedDate);
        }

        return date;
    }

    /**
     * Regex to remove unneeded characters to help filtering
     *
     * @param number String to be trimmed
     * @return The phone number
     */
    public static String simplePhone(String number) {
        return number.replaceAll("((\\+1)?[\\- ()\\.]*)", "");
    }

    public static Var.MsgType getMsgType(String type) {

        switch (type) {
            case "1":
                return Var.MsgType.RECEIVED;
            case "2":
                return Var.MsgType.SENT;
            case "3":
                return Var.MsgType.DRAFT;
        }

        return Var.MsgType.OTHER;
    }

    public static boolean validateURI(String uri) {
        final URI u;
        try {
            u = URI.create(uri);
        } catch (Exception e) {
            return false;
        }
        return true;
    }


    public static ArrayList<SmsData> sortedSmsData(HashMap<Long, SmsData> smsDataList) {
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

}
