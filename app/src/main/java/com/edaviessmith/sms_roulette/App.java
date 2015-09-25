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

                        phone.setValue(simplePhone(number));
                        contact.getInfo().add(phone);
                    }

                    pCur.close();
                }

                contactList.add(contact);
            }
        }
        cur.close();

    }

    public static String simplePhone(String number) {
        //return number.replace("-","");
        return number.replaceAll("([ -()]*)", "");
    }


    /**
     * Iterate cursor through sms messages and populate conversationList
     * @conversation number filter the sms messages that contain the address
     *               or the newest for each unique address if null
     */
    public void readConversations(Conversation conversation) {
        /* Add Sms to contacts */

        /**
         * TODO: Use SQL to get the most recent message for each number
         SELECT * FROM  `message`
         WHERE DATE
         IN (SELECT MAX( DATE )
         FROM  `message`
         GROUP BY phone)
         */

        String selection = (conversation != null && conversation.getNumber() != null)?
                               "address LIKE '" + conversation.getNumber() + "'":
                               "date IN (SELECT MAX( date ) FROM  sms GROUP BY address)";

        Uri sms_uri = Uri.parse("content://sms/");
        String sortSmsOrder = "address ASC, date " + (conversation != null? "ASC":"DESC");
        /* Get the most recent message for each address */
        Cursor c = getContentResolver().query(sms_uri, null, selection, null, null);

        // Read the sms data and store it in the list
        if(c.moveToFirst()) {

            /* If the conversation is null create placeholder data, otherwise use it's info */
            String previousNumber = conversation != null? conversation.getNumber(): "";
            if(conversation == null) conversation = new Conversation();

            int index = 1;

            for(int i = 0; i < c.getCount(); i++) {

                SMSData sms = new SMSData();

                sms.setId(c.getInt(c.getColumnIndexOrThrow("_id")));
                sms.setNumber(c.getString(c.getColumnIndexOrThrow("address")));
                sms.setBody(c.getString(c.getColumnIndexOrThrow("body")));

                Long milis = c.getLong(c.getColumnIndexOrThrow("date")) / 1000;
                //TODO: change date to Cal whenever
                //Calendar.getInstance().setTimeInMillis(milis);
                sms.setDate(new Date(Long.valueOf(milis)));


                if(!previousNumber.equals(sms.getNumber())) {

                    boolean hasContact = false;

                    findContact:
                    for (Contact contact : contactList) {
                        if (contact.getInfo() != null) {
                            for (Info phoneInfo : contact.getInfo()) {
                                if (phoneInfo.getValue().equals(sms.getNumber())) {
                                    /* The contact's number matches, set the contact */
                                    conversation.setContact(contact);
                                    hasContact = true;
                                    break findContact;
                                }
                            }
                        }
                    }

                    /* Check if a previous conversation has the same phone */
                    findNumber:
                    if(!hasContact) {
                        for (Conversation conv : conversationList.values()) {
                            if(conv.getNumber().equals(sms.getNumber())) {
                                conversation = conv;
                                break findNumber;
                            }
                        }

                        conversation = new Conversation();
                        conversation.setNumber(sms.getNumber());
                    }

                    /* Add the conversation to the list if it isn't already there (Redundant Check) */
                    if(!conversationList.containsValue(conversation)) {
                        conversation.setId(index);  /* Add Map Id for bidirectionallity */
                        conversationList.put(index++, conversation);
                    }
                }

                /* Add Sms to the conversation */
                conversation.addSmsData(sms);
                previousNumber = sms.getNumber();

                c.moveToNext();
            }
        }

        c.close();
    }






}
