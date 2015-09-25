package com.edaviessmith.sms_roulette;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Var {
    public enum Category {PHONE}


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

}
