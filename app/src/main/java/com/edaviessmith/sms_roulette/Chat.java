package com.edaviessmith.sms_roulette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        int conversationKey = intent.getIntExtra("conversation", 0);
        conversation = app.getConversationList().get(conversationKey);

        //TODO async conversation
        chatState = Var.Feed.PENDING;
        new RetrieveChatTask(Chat.this, Var.LIMIT).execute(conversation);

        conversationAdapter = new ConversationAdapter(this);
        conversation_lv = (ListView) findViewById(R.id.conversation_lv);
        conversation_lv.setAdapter(conversationAdapter);

        conversation_lv.setOnScrollListener(new AbsListView.OnScrollListener() {


            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                /* Count is to 0 because the ListView stacks from the bottom */
                if (chatState == Var.Feed.IDLE && (firstVisibleItem - (totalItemCount / 4)) < 0) {
                    chatState = Var.Feed.PENDING;

                    int limit = Math.min(120, totalItemCount);
                    new RetrieveChatTask(Chat.this, limit).execute(conversation);
                    //conversation_lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
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
        public int getCount() {
            return smsDataList.size();
        }

        @Override
        public SmsData getItem(int position) {
            //return smsDataList.get(position);
            return smsDataList.get((getCount() - 1) - position);
        }

        @Override
        public int getViewTypeCount() {
            return 2;   // Sent and Receive
        }

        @Override
        public int getItemViewType(int position) {
            return getItem(position).getType() == Var.MsgType.SENT ? 0 : 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if(convertView == null) {

                if (getItemViewType(position) == 0)
                    convertView = inflater.inflate(R.layout.item_chat, parent, false);
                else
                    convertView = inflater.inflate(R.layout.item_chat_receive, parent, false);

                ViewHolder holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            final ViewHolder holder = (ViewHolder) convertView.getTag();

            SmsData smsData = getItem(position);

            holder.name_tv.setText(smsData.getBody());
            holder.date_tv.setText(Var.getTimeSince(smsData.getDate()));


            if(smsData.getType() == Var.MsgType.SENT) {
                holder.bubble_v.setBackgroundResource(R.drawable.bubble);

                if (position == getCount() - 1 || getItem(position + 1).getType() == Var.MsgType.RECEIVED) {
                    if (conversation.getContact() != null) {
                        holder.photo_iv.setImageBitmap(app.ownerPhoto);
                    } else {
                        holder.photo_iv.setImageResource(R.drawable.ic_launcher);
                    }
                    holder.photo_iv.setVisibility(View.VISIBLE);
                } else {
                    holder.photo_iv.setVisibility(View.GONE);
                }

            } else {
                holder.bubble_v.setBackgroundResource(R.drawable.bubble2);

                if (position == getCount() - 1 || getItem(position + 1).getType() == Var.MsgType.SENT) {
                    if (conversation.getContact() != null) {
                        Bitmap b = app.getPhotoFromUri(conversation.getContact());
                        holder.photo_iv.setImageBitmap(b);
                    } else {
                        holder.photo_iv.setImageResource(R.drawable.ic_launcher);
                    }
                    holder.photo_iv.setVisibility(View.VISIBLE);
                } else {
                    holder.photo_iv.setVisibility(View.GONE);
                }
            }

            return convertView;

        }

        class ViewHolder {
            TextView name_tv,
                    date_tv,
                    message_tv;
            View bubble_v;
            ImageView photo_iv;

            public ViewHolder(View view) {
                message_tv = (TextView) view.findViewById(R.id.message_tv);
                name_tv = (TextView) view.findViewById(R.id.name_tv);
                date_tv = (TextView) view.findViewById(R.id.date_tv);
                bubble_v = view.findViewById(R.id.bubble_v);
                photo_iv = (ImageView) view.findViewById(R.id.photo_iv);
            }
        }

    }

    static class RetrieveChatTask extends AsyncTask<Conversation, Void, Void> {

        private final Chat chat;
        private final int limit;
        private List<SmsData> smsDataList;

        public RetrieveChatTask(Chat chat, int limit) {
            this.chat = chat;
            this.limit = limit;
        }

        @Override
        protected Void doInBackground(Conversation... conversations) {

            List<SmsData> nextSmsList = chat.app.readConversations(conversations[0], limit);

            /* Add data to the conversation list and get a list of smsData that has not been added yet */
            smsDataList = chat.conversation.checkSmsData(nextSmsList);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            // Assumes list is older (might be an okay assumption)
            int index = chat.conversation_lv.getFirstVisiblePosition() + smsDataList.size();
            View v = chat.conversation_lv.getChildAt(chat.conversation_lv.getHeaderViewsCount());
            int top = (v == null) ? 0 : v.getTop();

            chat.chatState = smsDataList.isEmpty() ? Var.Feed.DONE : Var.Feed.IDLE;
            chat.conversationAdapter.smsDataList.addAll(smsDataList);

            chat.conversationAdapter.notifyDataSetChanged();

            // Use the previous position plus newer items to fake the offset not moving the ListView
            chat.conversation_lv.setSelectionFromTop(index, top);
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
