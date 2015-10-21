package com.edaviessmith.sms_roulette;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.edaviessmith.sms_roulette.data.Conversation;
import com.edaviessmith.sms_roulette.data.SmsData;
import com.edaviessmith.sms_roulette.view.RevolverView;

import java.util.Date;
import java.util.List;


public class Chat extends ActionBarActivity {

    App app;

    Conversation conversation;

    public ConversationAdapter conversationAdapter;
    public RecyclerView conversation_lv;
    public LinearLayoutManager linearLayoutManager;


    RevolverView revolverView;

    ImageView bullet_iv;

    Var.Feed chatState = Var.Feed.IDLE;

    boolean isSendingMsg;

    Animation transAnim;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_chat);

        app = (App) getApplication();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bullet_iv = (ImageView) findViewById(R.id.bullet_iv);

/*        bullet_iv.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                       int oldTop, int oldRight, int oldBottom) {
                transAnim = new TranslateAnimation(0f, 0f, v.getHeight(), 0f);
                transAnim.setDuration(150);
                transAnim.setInterpolator(new DecelerateInterpolator());
            }
        });*/


        revolverView = (RevolverView) findViewById(R.id.revolver_v);
        revolverView.setOnFireListener(new Listener() {

            @Override
            public void onProgress(final float percent) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(isSendingMsg) {
                            if(percent < 0.2f) isSendingMsg = false;
                        } else {

                            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) conversation_lv.getLayoutParams();
                            int mar = (int) ((1f - percent) * -60);
                            //params.bottomMargin = mar;


                            bullet_iv.setVisibility(View.GONE);

                            if(transAnim != null) transAnim.cancel();

                        }
                    }
                });
            }

            @Override
            public void onComplete() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String msgText = "This is a test so when I am deving I don't have to continually type a message. It's long to get the full effect of the animation";
                        //TODO in order
                        // create the message, add it to the lv, scroll it down

                        //conversation_lv.setSelection(conversationAdapter.getCount() - 1);

                        SmsData newMessage = new SmsData();
                        newMessage.setBody(msgText);
                        newMessage.setDate(new Date().getTime());
                        newMessage.setType(Var.MsgType.SENDING);

                        conversationAdapter.sendSmsMessage(newMessage);

                        // show the bullet view


                        // start animation for msg and bullet

                        isSendingMsg = true;
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

        linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        conversation_lv.setLayoutManager(linearLayoutManager);
        conversation_lv.setItemAnimator(new DefaultItemAnimator());


        conversationAdapter = new ConversationAdapter(this);
        conversation_lv.setAdapter(conversationAdapter);

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


    public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

        private LayoutInflater inflater;
        public List<SmsData> smsDataList;
        private Context context;

        public ConversationAdapter(Context context) {
            this.context = context;
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            smsDataList = Var.sortedSmsData(conversation.getSmsDataList());
        }

        public void sendSmsMessage(SmsData smsMessage) {
            smsDataList.add(0, smsMessage);
            conversation_lv.scrollToPosition(smsDataList.size() - 1);

        }



        private static final int TYPE_RECEIVED = 0;
        private static final int TYPE_SENT = 1;
        private static final int TYPE_SHOT = 2;


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = null;
            /*if (i == TYPE_RECEIVED) v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_receive, viewGroup, false);
            else if (i == TYPE_SENT) v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat, viewGroup, false);
            else if (i == TYPE_SHOT) v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat, viewGroup, false);*/

            if (i == TYPE_RECEIVED)
                v = inflater.inflate(R.layout.item_chat_receive, viewGroup, false);
            else
                v = inflater.inflate(R.layout.item_chat, viewGroup, false);
            // LayoutInflater.from(viewGroup.getContext())

            if(v != null) v.setOnClickListener(this);
            return new ViewHolder(v);
        }

        @Override
        public int getItemViewType(int position) {

            //if(getItem(position).getType() == Var.MsgType.SENDING) return TYPE_SENT;
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

            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
                                           int oldTop, int oldRight, int oldBottom) {

                    if (position > lastPosition) {
                        //TODO (animation)
                        //create a view listener to take the calculated height and start the bullet and slide animation
                        int height = view.getHeight();

                        // BULLET ANIMATION
                        transAnim = new TranslateAnimation(0f, 0f, height, 0f);
                        transAnim.setDuration(1500);
                        transAnim.setInterpolator(new DecelerateInterpolator());

                        bullet_iv.getLayoutParams().height = height;
                        bullet_iv.startAnimation(transAnim);
                        bullet_iv.setVisibility(View.VISIBLE);


                        //LIST ANIMATION
                        //TODO translate looks ugly and collapsing has issues with current layout params
                        //collapseView(conversation_lv, conversation_lv.getHeight() - height);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) conversation_lv.getLayoutParams();
                        //params.removeRule(RelativeLayout.ABOVE);
                        //params.addRule(RelativeLayout.ABOVE, -1);
                        conversation_lv.getLayoutParams().height = conversation_lv.getHeight() + height;
                        conversation_lv.startAnimation(transAnim);


                        //MESSAGE ANIMATION
                        // If the bound view wasn't previously displayed on screen, it's animated
                        Animation animation = new TranslateAnimation(view.getWidth(), 0, 0, 0);
                        animation.setStartOffset(1500);
                        animation.setDuration(1000);
                        animation.setAnimationListener(new Animation.AnimationListener() {
                            @Override
                            public void onAnimationStart(Animation animation) {
                                view.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) conversation_lv.getLayoutParams();
                                params.addRule(RelativeLayout.ABOVE, revolverView.getId());
                                conversation_lv.clearAnimation();*/
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }
                        });

                        view.setVisibility(View.INVISIBLE);
                        view.startAnimation(animation);

                        lastPosition = position;
                    }
                }
            });

        }


        // COLLAPSING ANIMATION (should move somewhere cleaner)

        /**
         * Slide animation
         *
         * @param start   start animation from position
         * @param end     end animation to position
         * @param summary view to animate
         * @return valueAnimator
         */
        private ValueAnimator slideAnimator(int start, int end, final View summary) {

            ValueAnimator animator = ValueAnimator.ofInt(start, end);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    //Update Height
                    int value = (Integer) valueAnimator.getAnimatedValue();

                    ViewGroup.LayoutParams layoutParams = summary.getLayoutParams();
                    layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, value, getResources().getDisplayMetrics());//value;
                    summary.setLayoutParams(layoutParams);
                }
            });
            return animator;
        }

        private void collapseView(final View summary, int height) {
            int finalHeight = summary.getHeight();

            ValueAnimator mAnimator = slideAnimator(finalHeight, height, summary);
            final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.EXACTLY);
            summary.measure(widthSpec, height);

            Animator animator = slideAnimator(summary.getHeight(), height, summary);
            animator.start();
            mAnimator.start();
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

            // Use the previous position plus newer items to fake the offset not moving the ListView
            //chat.conversation_lv.setSelectionFromTop(index, top);
        }
    }


/*    @Override
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
    }*/
}
