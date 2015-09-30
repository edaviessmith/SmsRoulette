package com.edaviessmith.sms_roulette;

import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

import com.edaviessmith.sms_roulette.data.Contact;
import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.Info;
import com.edaviessmith.sms_roulette.data.SMSData;

import java.io.InputStream;
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



    public Bitmap getPhotoFromUri(Contact contact) {

        /*AssetFileDescriptor afd = null;
        try {

            //Uri thumbUri;
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //thumbUri = Uri.parse(photo);
            *//*} else {
                final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_URI, photoData);
                thumbUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);
            }*//*
            ContentResolver cr = context.getContentResolver();
            afd = cr.openAssetFileDescriptor(photo, "r");
            FileDescriptor fileDescriptor = afd.getFileDescriptor();
            if (fileDescriptor != null)
                return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, null);
        } catch (FileNotFoundException e) {
        } finally {
            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {
                }
            }
        }*/

        //return null;

        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(), photoUri);
        if (input != null) {
            return BitmapFactory.decodeStream(input);
        }

        /*if (photo != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), photo);
            if (input != null) {
                return BitmapFactory.decodeStream(input);
            }
        }*/

        return BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_report_image);
    }




    public void readContacts() {

        /* Add contacts with numbers */
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cur = cr.query(uri, null, null, null, sortOrder);
        if(cur.getCount() > 0)
        {
            long id;
            String name;
            String thumbUri;
            String photoUri;

            while(cur.moveToNext())
            {
                Contact contact = new Contact();
                id       = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
                name     = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                thumbUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                photoUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                contact.setId(id);
                contact.setDisplayName(name);

                if(Var.validateURI(thumbUri)) contact.setThumbUri(Uri.parse(thumbUri));
                if(Var.validateURI(photoUri)) contact.setPhotoUri(Uri.parse(photoUri));


                if (Integer.parseInt(cur.getString(cur.getColumnIndex
                        (ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{String.valueOf(id)}, null);

                    int type;
                    String number;

                    while (pCur.moveToNext()) {

                        Info phone = new Info(Var.Category.PHONE);
                        type   = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                        number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        phone.setType(type);
                        phone.setValue(Var.simplePhone(number));

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
     * @conversation number filter the sms messages that contain the address
     *               or the newest for each unique address if null
     */
    public void readConversations(Conversation conversation) {
        /* Add Sms to contacts */

        /**
         * TODO: Use SQL to get the most recent message for each number
         SELECT * FROM  `message`
         WHERE DATE IN (SELECT MAX( DATE )
                        FROM  `message`
                        GROUP BY phone)
         */

        String selection;

        if(conversation != null && conversation.getNumber() != null) {

            StringBuilder sb = new StringBuilder();
            for(String s: conversation.getRawNumbers()) {
                sb.append(s).append("', '");
            }

            selection = "address IN ('" + sb.substring(0, sb.length() - 3) + ")";
            if(!conversation.getSmsDataList().isEmpty()) {
                selection += " AND date < " + conversation.getSmsDataList().get(conversation.getSmsDataList().size() - 1);
            }
        } else {
            selection = "date IN (SELECT MAX( date ) FROM  sms GROUP BY address)";
        }

        Uri sms_uri = Uri.parse("content://sms/");

        //TODO: SQL pagination needed to quicken queries and loading

        String sortSmsOrder = "address ASC, date " + (conversation != null? "ASC":"DESC");


        /* Get the most recent message for each address */
        Cursor c = getContentResolver().query(sms_uri, null, selection, null, null);

        // Read the sms data and store it in the list
        if(c.moveToFirst()) {

            /* If the conversation is null create placeholder data, otherwise use it's info */
            String previousNumber = conversation != null? conversation.getNumber(): "";
            if(conversation == null) conversation = new Conversation();

            int index = 1;

            int id;
            String body, address;
            Var.MsgType type;
            Long date;

            for(int i = 0; i < c.getCount() && i <= Var.LIMIT; i++) {

                SMSData sms = new SMSData();

                id      = c.getInt(c.getColumnIndexOrThrow("_id"));
                body    = c.getString(c.getColumnIndexOrThrow("body"));
                type    = Var.getMsgType(c.getString(c.getColumnIndexOrThrow("type")));
                address = c.getString(c.getColumnIndexOrThrow("address"));
                date    = c.getLong(c.getColumnIndexOrThrow("date")) / 1000;

                sms.setId(id);
                sms.setBody(body);
                sms.setType(type);
                sms.setNumber(address);
                sms.setDate(new Date(date));

                //TODO: change date to Cal whenever
                //Calendar.getInstance().setTimeInMillis(milis);

                if(!previousNumber.equals(sms.getNumber())) {

                    boolean hasConversation = false;

                    /* Check if a previous conversation has the same phone */
                    for (Conversation conv : conversationList.values()) {
                        if(conv.getNumber().equals(sms.getNumber())) {
                            conversation = conv;

                            hasConversation = true;
                            break;
                        }
                    }

                    findContact:
                    if(!hasConversation) {

                        /* Create a new conversation */
                        conversation = new Conversation();
                        conversation.setNumber(sms.getNumber());

                        /* Search for a matching contact */
                        for (Contact contact : contactList) {
                            if (contact.getInfo() != null) {
                                for (Info phoneInfo : contact.getInfo()) {
                                    if (phoneInfo.getValue().equals(sms.getNumber())) {
                                        /* The contact's number matches, set the contact and break */
                                        conversation.setContact(contact);

                                        break findContact;
                                    }
                                }
                            }
                        }

                    }

                    /* Add the conversation to the list if it isn't already there (Redundant Check) */
                    if(!conversationList.containsValue(conversation)) {
                        //TODO: index should not be used (too much thought and slow)
                        conversation.setId(index);  /* Add Map Id for bidirectionallity */
                        conversationList.put(index++, conversation);
                    }
                }

                /* Add Sms to the conversation */
                conversation.addRawNumber(address);
                conversation.addSmsData(sms);
                previousNumber = sms.getNumber();

                c.moveToNext();
            }
        }

        c.close();
    }


}
