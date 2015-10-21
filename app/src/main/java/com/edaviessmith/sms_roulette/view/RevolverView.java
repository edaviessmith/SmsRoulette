package com.edaviessmith.sms_roulette.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.edaviessmith.sms_roulette.Listener;
import com.edaviessmith.sms_roulette.R;
import com.edaviessmith.sms_roulette.Revolver;

/**
 * Created by Ethan on 03/10/2015.
 */
public class RevolverView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceThread thread;
    Revolver rev;

    Bitmap bm_revolver, bm_trigger;
    Bitmap bitmap;
    Canvas temp;

    private boolean isTouching;
    public int touchDownPosX;

    public final int touchDownDragLeft = 100;
    private Listener onFireListener;


    //Measure frames per second.
    /*long now;
    int framesCount = 0;
    int framesCountAvg = 0;
    long framesTimer = 0;
    Paint fpsPaint = new Paint();

    //Frame speed
    long timeNow;
    long timePrev = 0;
    long timePrevFrame = 0;
    long timeDelta;*/


    public RevolverView(Context context) {
        super(context);
        init();
    }

    public RevolverView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RevolverView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }


    private void init() {
        //fpsPaint.setTextSize(30);
        transparentPaint = new Paint();
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        transparentPaint.setAntiAlias(true);

        this.setBackgroundColor(Color.TRANSPARENT);
        this.setZOrderOnTop(true); //necessary
        getHolder().setFormat(PixelFormat.TRANSPARENT);

        //Set thread
        getHolder().addCallback(this);
        setFocusable(true);


    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //This event-method provides the real dimensions of this custom view.
        Point screen = new Point(w, h);

        rev = new Revolver(screen);


        //TODO: decode bitmap with aspect ratio, then create a scaled bitmap
        BitmapFactory.Options revOpts = new BitmapFactory.Options();

        bm_revolver = BitmapFactory.decodeResource(getResources(), R.drawable.revolver); //Load a bm_revolver image.
        bm_trigger = BitmapFactory.decodeResource(getResources(), R.drawable.trigger); //Load a background.

        bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        temp = new Canvas(bitmap);
    }


    // Pass the touch event on to another view (allows overlapping)
    private boolean isTouchDisable = false;


    public boolean dispatchTouchEvent(MotionEvent ev) {
        // although the ScrollView doesn't get touch events , its children will get them so intercept them.
        if (isTouchDisable) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {


        if(ev.getX() > (rev.screen.x - touchDownDragLeft)) {
            rev.animStep = 1f;
        } else {

            Log.d("touch", "down " + ev.getY());
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    touchDownPosX = (int) ev.getX();
                    isTouching = true;

                    if(ev.getY() < 100) {
                        isTouchDisable = true;
                    } else {
                        isTouchDisable = false;
                    }

                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (ev.getX() > touchDownPosX)
                        rev.animStep = (ev.getX() - touchDownPosX) /
                                     (rev.screen.x - touchDownPosX - touchDownDragLeft);

                    if(ev.getY() < 100) {
                        isTouchDisable = true;
                    } else {
                        isTouchDisable = false;
                    }

                    break;
                }

                case MotionEvent.ACTION_UP:
                    isTouching = false;
                    break;
            }
        }

        // no more touch events for this view
        if (isTouchDisable) {
            return false;
        }


        return true;
    }


    Paint transparentPaint;
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //temp.drawColor(Color.argb(80, 0, 0, 0));

        temp.drawRect(rev.screen.x, rev.screen.y, 0, 0, transparentPaint);

        if(canvas != null) {
            //canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);


            if(!isTouching) {
                rev.touchUpAnim();
            }

            rev.stepFireAnimation();


            // TRIGGER
            temp.save(); //Save the position of the canvas matrix.
            rev.transformTrigger(temp);

            temp.drawBitmap(bm_trigger, 0, 0, null);
            temp.restore(); //Rotate the canvas matrix back to its saved position

            // REVOLVER
            temp.save(); //Save the position of the canvas matrix.
            rev.transformRevolver(temp);

            temp.drawBitmap(bm_revolver, 0, 0, null);
            temp.restore(); //Rotate the canvas matrix back to its saved position


            //Measure frame rate (unit: frames per second).
            /*now = System.currentTimeMillis();
            canvas.drawText(framesCountAvg + " fps", 10, 10, fpsPaint);
            framesCount++;
            if (now - framesTimer > 1000) {
                framesTimer = now;
                framesCountAvg = framesCount;
                framesCount = 0;
            }*/

            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        if (onFireListener != null) onFireListener.onProgress(rev.animStep);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        setWillNotDraw(false);

        //Create thread to draw if non existent or it has been terminated
        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new SurfaceThread(getHolder(), this);
        }

        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
        boolean retry = true;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        rev.screen.set(width, height);
    }

    public void setOnFireListener(Listener onFireListener) {
        this.onFireListener = onFireListener;
    }


    public class SurfaceThread extends Thread {

        private SurfaceHolder surfaceHolder;
        private RevolverView surfaceView;

        private boolean running = false;

        public SurfaceThread(SurfaceHolder surfaceHolder, RevolverView surfaceView) {
            this.surfaceHolder = surfaceHolder;
            this.surfaceView = surfaceView;
        }

        public void setRunning(boolean run) {
            running = run;
        }

        @Override
        public void run() {
            super.run();
            Canvas canvas;
            while(running) {
                if(surfaceHolder.getSurface().isValid()) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas();
                        synchronized (surfaceHolder) {
                            surfaceView.postInvalidate();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }



                    if(rev.animStep == 1f) {
                        if(!rev.hasFired) {
                            rev.hasFired = true;
                            if (onFireListener != null) onFireListener.onComplete();
                        }
                    } else {
                        rev.hasFired = false;
                    }

                }
            }

        }

    }
}
