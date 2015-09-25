package com.edaviessmith.sms_roulette;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.SMSData;

import java.util.ArrayList;


public class Chat extends ActionBarActivity {

    App app;
    ArrayList<SMSData> conversationList;
    ConversationAdapter conversationAdapter;
    ListView conversation_lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_conversation);

        app = (App) getApplication();


        Intent intent = getIntent();
        int conversationKey = intent.getIntExtra("conversation", 0);
        Conversation conversation = app.getConversationList().get(conversationKey);

        app.readConversations(conversation.getPhone());

        conversationList = new ArrayList<>();
        conversationList.clear();
        conversationList.addAll(conversation.getSmsDataList());

        conversation_lv = (ListView) findViewById(R.id.conversation_lv);
        conversationAdapter = new ConversationAdapter(this);
        conversation_lv.setAdapter(conversationAdapter);
    }


    public class ConversationAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public ConversationAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return conversationList.size();
        }

        @Override
        public SMSData getItem(int position) {
            return conversationList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = inflater.inflate(R.layout.item_conversation, parent, false);
                ViewHolder holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();

            SMSData smsData = getItem(position);


             holder.name_tv.setText(smsData.getBody());


            return convertView;

        }

        class ViewHolder {
            TextView name_tv,
                    date_tv,
                    message_tv;

            public ViewHolder(View view) {
                message_tv = (TextView) view.findViewById(R.id.message_tv);
                name_tv = (TextView) view.findViewById(R.id.name_tv);
                date_tv = (TextView) view.findViewById(R.id.date_tv);
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
