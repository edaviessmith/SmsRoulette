package com.edaviessmith.sms_roulette;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Patterns;

import com.edaviessmith.sms_roulette.data.Contact;
import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.Info;
import com.edaviessmith.sms_roulette.data.SmsData;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Ethan on 24/09/2015.
 */
public class App extends Application {

    /**
     * App is a singleton instance used to retain data and state between activities
     */

    private static Context context;

    Handler handler;

    LinkedHashMap<Integer, Conversation> conversationList;
    List<Contact> contactList;
    Contact ownerContact;
    Bitmap ownerPhoto;


    // CONTEXT
    public void onCreate() {
        super.onCreate();
        App.context = getApplicationContext();

        conversationList = new LinkedHashMap<>();
        contactList = new ArrayList<>();

        //TODO move these init calls into an async thread to not stop app loading
        readContacts();

        if (ownerContact != null) {
            ownerPhoto = getPhotoFromUri(ownerContact);
        }

        queryConversations();

        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, new SmsContentObserver(handler));
    }

    public static Context getContext() {
        return App.context;
    }

    public List<Contact> getContactList() {
        return contactList;
    }

    public HashMap<Integer, Conversation> getConversationList() {
        return conversationList;
    }



    public Bitmap getPhotoFromUri(Contact contact) {

        Uri photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(), photoUri);
        if (input != null) {
            return BitmapFactory.decodeStream(input);
        }

        return BitmapFactory.decodeResource(getResources(), android.R.drawable.ic_menu_report_image);
    }


    public void readContacts() {

        /* Add contacts with numbers */
        Uri uri = ContactsContract.Contacts.CONTENT_URI;
        ContentResolver cr = getContentResolver();
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        Cursor cur = cr.query(uri, null, null, null, sortOrder);
        List<String> emailList = getOwnerEmails();

        if (cur.getCount() > 0) {

            while (cur.moveToNext()) {

                Contact contact = new Contact(cur);

                /* PHONE NUMBERS */

                if (Integer.parseInt(cur.getString(cur.getColumnIndex
                        (ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{String.valueOf(contact.getId())}, null);

                    while (pCur.moveToNext()) {

                        Info phone = new Info(Var.Category.PHONE, pCur);
                        contact.getInfo().add(phone);
                    }

                    pCur.close();
                }

                /* EMAILS */

                Cursor eCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{String.valueOf(contact.getId())}, null);

                while (eCur.moveToNext()) {

                    Info email = new Info(Var.Category.EMAIL, eCur);
                    contact.getInfo().add(email);

                    /* Check if this email belongs to the main google account */
                    if (ownerContact == null && emailList.contains(email.getValue())) {
                        //TODO We're finding the owner, but the photo is not working
                        ownerContact = contact;

                    }

                }
                eCur.close();

                contactList.add(contact);
            }
        }
        cur.close();
    }


    /**
     * Iterate cursor through sms content resolver and get a list of SmsData
     * @param conversation - filter the sms messages that contain the address
     *                     or the newest for each unique address if null
     * @param limit - cursor limit number of rows returned
     * @return List<SmsData>
     */
    public List<SmsData> readConversations(Conversation conversation, int limit) {
        String selection;
        List<SmsData> smsDataList = new ArrayList<>();

        if (conversation != null && conversation.getNumber() != null) {
            /* Build a query to include all variants of the conversation's numbers */
            StringBuilder sb = new StringBuilder();
            for (String s : conversation.getRawNumbers()) {
                sb.append(s).append("', '");
            }

            selection = "address IN ('" + sb.substring(0, sb.length() - 3) + ")";
            if (!conversation.getSmsDataList().isEmpty()) {
                selection += " AND date < " + conversation.getSmsDataList().get(conversation.getOldestMessage()).getDate();
            }
        } else {
            /* Build a query to get the newest conversation for each number (may include older messages with number variants */
            selection = "date IN (SELECT MAX( date ) FROM  sms GROUP BY address)";
        }

        //Log.i("App", "sql: " + selection);

        //TODO: SQL pagination needed to quicken queries and loading (just needs thorough check now)
        /* Get the most recent message for each address */
        Cursor c = getContentResolver().query(Uri.parse("content://sms/"), null, selection, null, null);

        // Read the sms data and store it in the list


        if (c.moveToFirst()) {

            for (int i = 0; i < c.getCount() && (limit == 0 || i <= limit); i++) {
                /* Instantiate the smsData object and add it to a list */
                smsDataList.add(new SmsData(c));

                c.moveToNext();
            }
        }
        c.close();

        return smsDataList;
    }


    /**
     * Iterate cursor through sms messages and populate conversationList
     */
    public void queryConversations() {

        List<SmsData> smsDataList = readConversations(null, 0);

        String previousNumber = "";
        Conversation conversation = new Conversation();

        int index = 1; // Index is dumb :(

        for (SmsData sms : smsDataList) {
            if (!previousNumber.equals(sms.getNumber())) {
                /* Get the reference or new conversation from the App conversationList */
                conversation = findConversationRelation(sms, index++);
            }
            conversation.addRawNumber(sms.getRawNumber());
            conversation.addSmsData(sms);
        }

    }


    /**
     * Create new or reference conversations in the App's singleton conversationList
     *
     * @param sms   SmsData (the number is the only important property here)
     * @param index temporary index for listview selection (gross and dirty)
     * @return Conversation - A new conversation (if no current) or reference the related
     * conversation from the right contact
     */
    private Conversation findConversationRelation(SmsData sms, int index) {

        Conversation conversation = null;
        boolean hasConversation = false;

        /* Check if a previous conversation has the same phone */
        for (Conversation conv : conversationList.values()) {
            if (conv.getNumber().equals(sms.getNumber())) {
                conversation = conv;

                hasConversation = true;
                break;
            }
        }

        findContact:
        if (!hasConversation) {

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
        if (!conversationList.containsValue(conversation)) {

            //TODO: index should not be used (too much thought and slow)
            conversation.setId(index);  /* Add Map Id for bidirectionallity */
            conversationList.put(index, conversation);
        }

        return conversation;
    }

    public List<String> getOwnerEmails() {
        List<String> emails = new ArrayList<>();

        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(context).getAccounts();

        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                emails.add(account.name);
            }
        }

        return emails;
    }


    public class SmsContentObserver extends ContentObserver {

        public SmsContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            //TODO check if there is a new message

            //
            /*bodies.clear();
            buildMessageList();
            items = sortBodies(bodies);
            runOnUiThread(new Runnable() {
                public void run() {
                    loadListView();
                }
            });*/

            super.onChange(selfChange);
        }
    }
}