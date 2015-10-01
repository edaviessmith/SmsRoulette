package com.edaviessmith.sms_roulette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.SmsData;

import java.util.List;


public class Chat extends ActionBarActivity {

    App app;

    Conversation conversation;

    ConversationAdapter conversationAdapter;
    ListView conversation_lv;


    Var.Feed chatState = Var.Feed.IDLE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_conversation);

        app = (App) getApplication();


        Intent intent = getIntent();
        int conversationKey = intent.getIntExtra("conversation", 0);
        conversation = app.getConversationList().get(conversationKey);

        //TODO async conversation
        chatState = Var.Feed.PENDING;
        new RetrieveChatTask(Chat.this).execute(conversation);

        conversationAdapter = new ConversationAdapter(this);
        conversation_lv = (ListView) findViewById(R.id.conversation_lv);
        conversation_lv.setAdapter(conversationAdapter);

        conversation_lv.setOnScrollListener(new AbsListView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (chatState == Var.Feed.IDLE && firstVisibleItem + visibleItemCount > totalItemCount / 2) {
                    chatState = Var.Feed.PENDING;

                    new RetrieveChatTask(Chat.this).execute(conversation);
                }
            }

        });
    }


    public class ConversationAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        public List<SmsData> smsDataList;

        public ConversationAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            smsDataList = conversation.sortedSmsData();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();

            smsDataList = conversation.sortedSmsData();
        }

        @Override
        public int getCount() {
            return smsDataList.size();
        }

        @Override
        public SmsData getItem(int position) {

            return smsDataList.get((getCount() - 1) - position);
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

            SmsData smsData = getItem(position);

            holder.name_tv.setText(smsData.getBody());
            holder.date_tv.setText(Var.getTimeSince(smsData.getDate()));

            if(smsData.getType() == Var.MsgType.SENT) {
                holder.name_tv.setTextColor(Color.BLUE);
            } else {
                holder.name_tv.setTextColor(Color.BLACK);
            }

            holder.message_tv.setVisibility(View.GONE);


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

    static class RetrieveChatTask extends AsyncTask<Conversation, Void, Void> {

        private final Chat chat;
        private List<SmsData> smsDataList;

        public RetrieveChatTask(Chat chat) {
            this.chat = chat;
        }

        @Override
        protected Void doInBackground(Conversation... conversations) {

            smsDataList = chat.app.readConversations(conversations[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //todo (maybe listener to know when to update the adapter)
            chat.chatState = Var.Feed.IDLE;

            chat.conversation.addSmsData(smsDataList);

            // Missing sooo much logic (damn my half thinking 2h into this once brilliant refactor)
            // *note refactor usually means having a substantial amount of code beforehand
            chat.conversationAdapter.smsDataList = chat.conversation.sortedSmsData();

            chat.conversationAdapter.notifyDataSetChanged();
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
