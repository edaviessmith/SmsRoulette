package com.edaviessmith.sms_roulette;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Var {
    public enum Category {PHONE}
    public enum MsgType {RECEIVED, SENT, DRAFT, OTHER}

    public static int LIMIT = 20;


    public static String getTimeSince(long publishedDate) {
        String date = "";

        if (publishedDate <= 0) {
            //date = context.getResources().getString(R.string.loading_date);
        } else {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(publishedDate);

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
            date += s.format(publishedDate * 1000);
        }

        return date;
    }

    /**
     * Regex to remove unneeded characters to help filtering
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

}
