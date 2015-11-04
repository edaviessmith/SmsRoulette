package com.edaviessmith.sms_roulette;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.SmsData;
import com.edaviessmith.sms_roulette.view.RevolverView;

import java.util.Date;
import java.util.List;


public class Chat extends ActionBarActivity implements View.OnClickListener{

    App app;
    Conversation conversation;

    public ConversationAdapter conversationAdapter;
    public RecyclerView conversation_lv;
    public LinearLayoutManager linearLayoutManager;

    RevolverView revolverView;

    ImageView bullet_iv, send_iv;
    EditText message_et;

    Var.Feed     chatState = Var.Feed.IDLE;
    Var.REV_STATE revState = Var.REV_STATE.IDLE;

    // View heights used in animations
    int conversationHeight, messageHeight;
    boolean isSendingMsg;

    Animation transAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        app = (App) getApplication();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bullet_iv = (ImageView) findViewById(R.id.bullet_iv);
        message_et = (EditText) findViewById(R.id.message_et);

        send_iv = (ImageView) findViewById(R.id.send_iv);
        send_iv.setOnClickListener(this);

        revolverView = (RevolverView) findViewById(R.id.revolver_v);
        revolverView.setOnFireListener(new Listener() {

            @Override
            public void onProgress(final float percent) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                       /* if(isSendingMsg) {
                            if(percent < 0.2f) isSendingMsg = false;
                        } else {
                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) conversation_lv.getLayoutParams();
                            int mar = (int) ((1f - percent) * -60);
                            //params.bottomMargin = mar;

                            //bullet_iv.setVisibility(View.GONE);
                            if(transAnim != null) transAnim.cancel();
                        }*/
                    }
                });
            }

            @Override
            public void onComplete() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        transitionState(Var.REV_STATE.FIRING);
                    }
                });
            }
        });

        Intent intent = getIntent();
        int conversationKey = intent.getIntExtra("conversation", 0);
        conversation = app.getConversationList().get(conversationKey);

        chatState = Var.Feed.PENDING;
        new RetrieveChatTask(Chat.this, Var.LIMIT).execute(conversation);


        conversation_lv = (RecyclerView) findViewById(R.id.conversation_lv);
        conversation_lv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                conversationHeight = conversation_lv.getHeight();
                conversation_lv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

       /* conversation_lv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
           @Override
           public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                      int oldTop, int oldRight, int oldBottom) {
               conversationHeight = conversation_lv.getHeight();
               conversation_lv.removeOnLayoutChangeListener(this);
           }
       });*/


        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);

        conversation_lv.setLayoutManager(linearLayoutManager);
        conversation_lv.setItemAnimator(new DefaultItemAnimator());

        conversationAdapter = new ConversationAdapter(this);
        conversation_lv.setAdapter(conversationAdapter);

        transitionState(Var.REV_STATE.IDLE);

        /*conversation_lv.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                *//* Count is to 0 because the ListView stacks from the bottom *//*
                if (chatState == Var.Feed.IDLE && (firstVisibleItem - (totalItemCount / 4)) < 0) {
                    chatState = Var.Feed.PENDING;

                    int limit = Math.min(120, totalItemCount);
                    new RetrieveChatTask(Chat.this, limit).execute(conversation);
                    //conversation_lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
                }
            }
        });*/

    }

    /**
     * Util method to check if the next state is the next transition
     * @return true if newState is the nextState and revState is the previous state
     */
    private boolean isNextState(Var.REV_STATE newState, Var.REV_STATE nextState) {

        if(newState == nextState) {
            switch (newState) {
                case LOADING:
                    return revState == Var.REV_STATE.IDLE;
                case FIRING:
                    return revState == Var.REV_STATE.LOADING;
                case SHOT:
                    return revState == Var.REV_STATE.FIRING;
                case IDLE:
                    return revState == Var.REV_STATE.SHOT;
            }
        }
        return false;
    }

    private void transitionState(Var.REV_STATE newState) {

        revolverView.setVisibility((newState == Var.REV_STATE.IDLE) ? View.GONE: View.VISIBLE);

        if(isNextState(newState, Var.REV_STATE.LOADING)) {

            //Dismiss keyboard
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(message_et.getWindowToken(), 0);

            //Animate Revolver up
            revolverView.setVisibility(View.VISIBLE);

            revolverView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Animation revAnim = new TranslateAnimation(0f, 0f, revolverView.getHeight(), 0f);
                    revAnim.setDuration(Var.translateTime);
                    revAnim.setInterpolator(new DecelerateInterpolator());
                    revolverView.startAnimation(revAnim);

                    //TODO removed animation to see if that obstructs setting height
                    //animatorConversation = app.collapseView(conversation_lv, (conversationHeight - revolverView.getHeight()) + Var.getDp(226));
                    app.setViewHeight(conversation_lv, (conversationHeight - revolverView.getHeight()) + Var.getDp(226));

                    revolverView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }

        if(isNextState(newState, Var.REV_STATE.FIRING)) {

            String msgText = "This is a test so when I am deving I don't have to continually type a message. It's long to get the full effect of the animation";

            // create the message, add it to the lv, scroll it down

            //conversation_lv.setSelection(conversationAdapter.getCount() - 1);

            SmsData newMessage = new SmsData();
            newMessage.setBody(msgText);
            newMessage.setDate(new Date().getTime());
            newMessage.setType(Var.MsgType.SENDING);

            conversationAdapter.sendSmsMessage(newMessage);

            isSendingMsg = true;
        }


        if(isNextState(newState, Var.REV_STATE.SHOT)) {

            //TODO convert the bullet (pole) into a 9 patch to hide the translating view (or find another way)
            // BULLET ANIMATION
            transAnim = new TranslateAnimation(0f, 0f, messageHeight, 0f);
            transAnim.setDuration(Var.translateTime);
            transAnim.setInterpolator(new DecelerateInterpolator());

            bullet_iv.getLayoutParams().height = messageHeight;
            bullet_iv.startAnimation(transAnim);
            bullet_iv.setVisibility(View.VISIBLE);

            //LIST ANIMATION
            final int height = conversation_lv.getHeight();
            app.setViewHeight(conversation_lv, height + messageHeight);

            conversation_lv.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    app.collapseView(conversation_lv, height);
                    conversation_lv.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });


        }

        if(isNextState(newState, Var.REV_STATE.IDLE)) {
            bullet_iv.setVisibility(View.GONE);
            //transAnim.cancel();

            //Hide the revolver view
            Animation revAnim = new TranslateAnimation(0f, 0f, 0f, revolverView.getHeight());
            revAnim.setDuration(Var.translateTime);
            revAnim.setInterpolator(new DecelerateInterpolator());
            revAnim.setAnimationListener(new AnimListener() {
                @Override
                public void onAnimationEnd(Animation animation) {
                    revolverView.reset();
                }
            });

            revolverView.startAnimation(revAnim);

            app.expandView(conversation_lv, conversationHeight, true);
        }

        revState = newState;
    }



    @Override
    public void onClick(View v) {

        if(send_iv == v) {
            transitionState(Var.REV_STATE.LOADING);
        }

    }


    public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

        private LayoutInflater inflater;
        public List<SmsData> smsDataList;

        public ConversationAdapter(Context context) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            smsDataList = Var.sortedSmsData(conversation.getSmsDataList());
        }

        public void sendSmsMessage(SmsData smsMessage) {
            smsDataList.add(0, smsMessage);
            conversation_lv.scrollToPosition(smsDataList.size() - 1);
            //notifyItemInserted(smsDataList.size() - 1);
        }


        private static final int TYPE_RECEIVED = 0;
        private static final int TYPE_SENT = 1;
        private static final int TYPE_SHOT = 2;


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v;

            if (i == TYPE_RECEIVED)
                v = inflater.inflate(R.layout.item_chat_receive, viewGroup, false);
            else
                v = inflater.inflate(R.layout.item_chat, viewGroup, false);

            if(v != null) v.setOnClickListener(this);
            return new ViewHolder(v);
        }

        @Override
        public int getItemViewType(int position) {

            if(getItem(position).getType() == Var.MsgType.RECEIVED) return TYPE_RECEIVED;

            return TYPE_SENT;
        }

        @Override
        public void onClick(final View view) {
            int itemPosition = conversation_lv.getChildPosition(view);
            /*if(itemPosition < getItemCount() - 1)  act.startVideo(getFeed().getItems().get(itemPosition));
            else if(feedState == Var.FEED_WARNING || feedState == Var.FEED_OFFLINE || feedState == Var.FEED_END) {
                setFeedState(Var.FEED_LOADING);
                new YoutubeFeedAsyncTask(act, getFeed(), userId, actionDispatch).execute(getFeed().getNextPageToken());

            }*/
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (viewHolder instanceof ViewHolder) {


                final ViewHolder holder = (ViewHolder) viewHolder;

                SmsData smsData = getItem(position);

                holder.name_tv.setText(smsData.getBody());
                holder.date_tv.setText(Var.getTimeSince(smsData.getDate()));


                if(smsData.getType() != Var.MsgType.RECEIVED) {
                    holder.bubble_v.setBackgroundResource(R.drawable.bubble);

                    if(smsData.getType() == Var.MsgType.SENDING) {

                        // Here you apply the animation when the view is bound
                        setAnimation(holder.container_v, position);
                    }

                }  else {
                    holder.bubble_v.setBackgroundResource(R.drawable.bubble2);

                    if (position == getCount() - 1 || getItem(position + 1).getType() == Var.MsgType.SENT) {
                        if (conversation.getContact() != null) {
                            Bitmap b = app.getPhotoFromUri(conversation.getContact());
                            holder.photo_iv.setImageBitmap(b);
                        } else {
                            holder.photo_iv.setImageResource(R.drawable.ic_person_grey600_36dp);
                        }
                        holder.photo_iv.setVisibility(View.VISIBLE);
                    } else {
                        holder.photo_iv.setVisibility(View.GONE);
                    }
                }

            }

        }



        private int lastPosition = -1;

        /**
         * Here is the key method to apply the animation
         */
        private void setAnimation(final View view, final int position) {

            view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

                @Override
                public boolean onPreDraw() {
                    if (position > lastPosition) {
                        // Create a view listener to take the calculated height and start the bullet and slide animation
                        messageHeight = view.getHeight();

                        transitionState(Var.REV_STATE.SHOT);

                        // MESSAGE ANIMATION

                        // If the bound view wasn't previously displayed on screen, it's animated
                        Animation animation = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                        animation.setStartOffset(1500);
                        animation.setDuration(Var.translateTime);

                        animation.setAnimationListener(new AnimListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                view.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                transitionState(Var.REV_STATE.IDLE);
                            }
                        });

                        view.setVisibility(View.INVISIBLE);
                        view.startAnimation(animation);


                        lastPosition = position;
                    }

                    view.getViewTreeObserver().removeOnPreDrawListener(this);

                    return false;
                }
            });

        }


        @Override
        public int getItemCount() {
            return smsDataList.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView  name_tv,
                      date_tv,
                      message_tv;
            View      container_v,
                      bubble_v;
            ImageView photo_iv;


            public ViewHolder(View view) {
                super(view);
                container_v =            view.findViewById(R.id.container_v);
                message_tv = (TextView)  view.findViewById(R.id.message_tv);
                name_tv    = (TextView)  view.findViewById(R.id.name_tv);
                date_tv    = (TextView)  view.findViewById(R.id.date_tv);
                bubble_v   =             view.findViewById(R.id.bubble_v);
                photo_iv   = (ImageView) view.findViewById(R.id.photo_iv);
            }
        }


        public SmsData getItem(int position) {
            // Read the list backwards (bottom is the newest)
            return smsDataList.get((getCount() - 1) - position);
        }

        public int getCount() {
            return smsDataList.size();
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

            // Assumes list is older (not an okay assumption)
            chat.chatState = smsDataList.isEmpty() ? Var.Feed.DONE : Var.Feed.IDLE;

            chat.conversationAdapter.smsDataList.addAll(smsDataList);
            chat.conversationAdapter.notifyDataSetChanged();

            chat.conversation_lv.scrollToPosition(chat.conversationAdapter.smsDataList.size() - 1);
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
