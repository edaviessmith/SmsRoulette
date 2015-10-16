package com.edaviessmith.sms_roulette.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.edaviessmith.sms_roulette.R;

/**
 * Created by Ethan on 03/10/2015.
 */
public class RevolverView extends SurfaceView implements SurfaceHolder.Callback {

    SurfaceThread thread;

    Point screen; //Device's screen size

    /*
    Hardcoded measurements of device
    r w 1100, h686
    t w 170, h188
    w 720, h 600
    */

    PointF revSize = new PointF(1.527f, 1.143f); //Revolver size relative to screen

    Point revPos;
    int revAngle;
    float revScale;

    //TODO: initial pos changes depending on screen width height
    PointF revPosIdle = new PointF(-0.4375f, 0.1944f);

    PointF trigScale = new PointF(0.2361f, 0.3133f);
    PointF trigOffset = new PointF(0.76f, 0.33f);
    int trigAngle = 0, trigAngleFire = -50;

    PointF revPosFire = new PointF(-0.0902f, 0.08333f);
    int revAngleFire = 60;
    float revScaleFire = 0.7f;


    float animStep = 0.0f;
    int touchDownPosX;
    final int touchDownDragLeft = 100;

    Bitmap revolver, trigger;

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

        //Set thread
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //This event-method provides the real dimensions of this custom view.
        screen = new Point(w, h);

        revPos = new Point((int) revPosIdle.x * screen.x,(int) revPosIdle.y * screen.y);

        //TODO: decode bitmap with aspect ratio, then create a scaled bitmap
        BitmapFactory.Options revOpts = new BitmapFactory.Options();

        revolver = BitmapFactory.decodeResource(getResources(), R.drawable.revolver); //Load a revolver image.
        trigger = BitmapFactory.decodeResource(getResources(), R.drawable.trigger); //Load a background.

    }


    @Override
    public synchronized boolean onTouchEvent(MotionEvent ev) {

        if(ev.getX() > (screen.x - touchDownDragLeft)) {
            animStep = 1f;
        } else {

            switch (ev.getAction()) {

                case MotionEvent.ACTION_DOWN: {
                    touchDownPosX = (int) ev.getX();
                    break;
                }

                case MotionEvent.ACTION_MOVE: {
                    if (ev.getX() > touchDownPosX)
                        animStep = (ev.getX() - touchDownPosX) / (screen.x - touchDownDragLeft);
                    break;
                }

                case MotionEvent.ACTION_UP:
                    break;
            }
        }

        Log.d("Revolver", "animStep " + animStep);
        return true;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        if(canvas != null) {
            canvas.drawColor(Color.WHITE);



            //if(animStep < 1f) animStep += 0.01f; else animStep = 0f;
            fireAnimation(animStep);

            //TODO get revolver center in simpler text
            float centerX = revPos.x + (scaleX(revSize.x) / 2);
            float centerY = revPos.y + (scaleY(revSize.y) / 2);


            // TRIGGER
            canvas.save(); //Save the position of the canvas matrix.
            canvas.translate(revPos.x, revPos.y);
            canvas.scale(revScale, revScale, centerX, centerY);
            canvas.rotate(revAngle, centerX, centerY); //Rotate the canvas matrix.

            //Rotate the canvas matrix.
            canvas.rotate(trigAngle, scaleX(trigOffset.x) + (scaleX(trigScale.x) / 2),  scaleY(trigOffset.y) + (scaleY(trigScale.y) / 2));

            //Draw the revolver by applying the canvas rotated matrix.
            canvas.drawBitmap(trigger,  scaleX(trigOffset.x), scaleY(trigOffset.y), null);
            canvas.restore(); //Rotate the canvas matrix back to its saved position - only the revolver bitmap was rotated not all canvas.

            // REVOLVER
            canvas.save(); //Save the position of the canvas matrix.
            canvas.translate(revPos.x, revPos.y);
            canvas.scale(revScale, revScale, centerX, centerY);
            canvas.rotate(revAngle, centerX, centerY); //Rotate the canvas matrix.

            canvas.drawBitmap(revolver, 0, 0, null);
            canvas.restore(); //Rotate the canvas matrix back to its saved position - only the revolver bitmap was rotated not all canvas.


            //Measure frame rate (unit: frames per second).
            /*now = System.currentTimeMillis();
            canvas.drawText(framesCountAvg + " fps", 10, 10, fpsPaint);
            framesCount++;
            if (now - framesTimer > 1000) {
                framesTimer = now;
                framesCountAvg = framesCount;
                framesCount = 0;
            }*/
        }
    }

    private float scaleX(float x) {
        return screen.x * x;
    }

    private float scaleY(float y) {
        return screen.y * y;
    }

    private void fireAnimation(float percent) {

        revPos.x = (int) ((scaleX(revPosFire.x - revPosIdle.x) * percent) + scaleX(revPosIdle.x));
        revPos.y = (int) ((scaleY(revPosFire.y - revPosIdle.y) * percent) + scaleY(revPosIdle.y));

        revAngle = (int) (revAngleFire * percent);
        revScale = (revScaleFire - 1) * percent + 1;

        trigAngle = (int) (trigAngleFire * percent);
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
        screen.x = width;
        screen.y = height;
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
                            //surfaceView.onDraw(canvas); // View must be invalidated to trigger layout refresh
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
                }
            }

        }

    }
}
