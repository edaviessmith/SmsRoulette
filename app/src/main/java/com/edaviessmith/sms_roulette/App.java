package com.edaviessmith.sms_roulette;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.edaviessmith.sms_roulette.data.Contact;
import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.Info;
import com.edaviessmith.sms_roulette.data.SMSData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Ethan on 24/09/2015.
 */
public class App extends Application {

    /**
     * App is a singleton instance used to retain data and state between activities
     *
     */

    private static Context context;


    LinkedHashMap<Integer, Conversation> conversationList;
    List<Contact> contactList;


    // CONTEXT
    public void onCreate(){
        super.onCreate();
        App.context = getApplicationContext();

        conversationList = new LinkedHashMap<>();
        contactList      = new ArrayList<>();


    }

    public static Context getContext() {
        return App.context;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public void setContactList(List<Contact> contactList) {
        this.contactList = contactList;
    }

    public HashMap<Integer, Conversation> getConversationList() {
        return conversationList;
    }

    public void setConversationList(LinkedHashMap<Integer, Conversation> conversationList) {
        this.conversationList = conversationList;
    }




    public void readContacts() {

        /* Add contacts with numbers */
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cur = cr.query(uri, null, null, null, sortOrder);
        if(cur.getCount() > 0)
        {
            String id;
            String name;
            while(cur.moveToNext())
            {
                Contact contact = new Contact();
                id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                contact.setId(id);
                contact.setDisplayName(name);

                if (Integer.parseInt(cur.getString(cur.getColumnIndex
                        (ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);

                    while (pCur.moveToNext()) {

                        Info phone = new Info(Var.Category.PHONE);
                        phone.setType(pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)));
                        String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phone.setValue(number);
                        contact.getInfo().add(phone);
                    }

                    pCur.close();
                }

                contactList.add(contact);
            }
        }
        cur.close();

    }


    /**
     * Iterate cursor through sms messages and populate conversationList
     * @param number filter the sms messages that contain the address
     *               or the newest for each unique address if null
     */
    public void readConversations(String number) {
        /* Add Sms to contacts */

        /**
         * TODO: Use SQL to get the most recent message for each number
         SELECT * FROM  `message`
         WHERE DATE
         IN (SELECT MAX( DATE )
         FROM  `message`
         GROUP BY phone)
         */

        String selection = (number != null)? "address = \"" + number + "\"":
                               "date IN (SELECT MAX( date ) FROM  sms GROUP BY address)";

        Uri sms_uri = Uri.parse("content://sms/");
        String sortSmsOrder = "address ASC, date DESC";
        /* Get the most recent message for each address */
        Cursor c = getContentResolver().query(sms_uri, null, selection, null, null);

        // Read the sms data and store it in the list
        if(c.moveToFirst()) {

            String previousNumber = "";
            Conversation conversation = new Conversation();
            int index = 1;

            for(int i = 0; i < c.getCount(); i++) {

                SMSData sms = new SMSData();
                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")));
                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")));

                String dt = c.getString(c.getColumnIndexOrThrow("date"));
                sms.setDate(new Date(Long.valueOf(dt)));


                if(!previousNumber.equals(sms.getNumber())) {

                    conversation = new Conversation();
                    conversation.setPhone(sms.getNumber());

                    findContact:
                    for (Contact contact : contactList) {
                        if (contact.getInfo() != null) {
                            for (Info phoneInfo : contact.getInfo()) {
                                if (phoneInfo.getValue().equals(sms.getNumber())) {
                                    /* The contact's number matches, set the contact */
                                    conversation.setContact(contact);
                                    break findContact;
                                }
                            }
                        }
                    }

                    conversation.setId(index);  /* Add Map Id for bidirectionallity */
                    conversationList.put(index++, conversation);
                }

                /* Add Sms to the conversation */
                conversation.getSmsDataList().add(sms);
                previousNumber = sms.getNumber();

                c.moveToNext();
            }
        }

        c.close();
    }

}
