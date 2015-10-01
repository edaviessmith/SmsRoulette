package com.edaviessmith.sms_roulette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.edaviessmith.sms_roulette.data.Conversation;


public class Messaging extends ActionBarActivity {

    App app;
    ListView conversation_lv;
    MessagingAdapter messagingAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_messaging);

        app = (App) getApplication();

        app.readContacts();
        app.queryConversations();


        //TODO: Sort conversations by the most recent message

        messagingAdapter = new MessagingAdapter(this);

        conversation_lv = (ListView) findViewById(R.id.messaging_lv);
        conversation_lv.setAdapter(messagingAdapter);
        conversation_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Conversation conversation = (Conversation) conversation_lv.getItemAtPosition(position);

                Intent i = new Intent(getApplicationContext(), Chat.class);
                i.putExtra("conversation", conversation.getId());
                startActivity(i);
            }
        });

    }

    public class MessagingAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        public MessagingAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return app.conversationList.size();
        }

        @Override
        public Conversation getItem(int position) {
            /* Ugly as sin, why go through the bother of having a sorted key/value when i can't get by index */
            return (Conversation) app.conversationList.values().toArray()[position];
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

            Conversation conversation = getItem(position);


            if(conversation.getContact() != null) {
                holder.name_tv.setText(conversation.getContact().getDisplayName());

                Bitmap b = app.getPhotoFromUri(conversation.getContact());
                holder.photo_iv.setImageBitmap(b);
            } else {
                holder.name_tv.setText(conversation.getNumber());

                holder.photo_iv.setImageResource(R.drawable.ic_launcher);
            }

            if (!conversation.getSmsDataList().isEmpty()) {
                holder.message_tv.setText(conversation.getSmsDataList().get(conversation.getNewestMessage()).getBody());
                holder.date_tv.setText(Var.getTimeSince(conversation.getSmsDataList().get(conversation.getNewestMessage()).getDate()));
            }


            return convertView;
        }

        class ViewHolder {
            TextView name_tv,
                    date_tv,
                    message_tv;
            ImageView photo_iv;

            public ViewHolder(View view) {
                message_tv = (TextView)  view.findViewById(R.id.message_tv);
                name_tv    = (TextView)  view.findViewById(R.id.name_tv);
                date_tv    = (TextView)  view.findViewById(R.id.date_tv);
                photo_iv   = (ImageView) view.findViewById(R.id.photo_iv);
            }
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_conversations, menu);
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
