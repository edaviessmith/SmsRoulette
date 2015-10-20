package com.edaviessmith.sms_roulette;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;

/**
 * Created by Ethan on 17/10/2015.
 */
public class Revolver {

    /**
     * Used to store the state, dimensions and any animation properties
     */

    /*
    Hardcoded measurements of device
    r w 1100, h686
    t w 170, h188
    w 720, h 600
    */

    public Point screen; //Device's screen size

    PointF revSize = new PointF(1.527f, 1.143f); //Revolver size relative to screen
    PointF trigSize = new PointF(0.2361f, 0.3133f);

    /* Current revolver bitmap transforms */
    public Point revPos;
    int revAngle;
    float revScale;

    /* Current trigger position bitmap transforms */
    public PointF trigOffset = new PointF(0.76f, 0.33f);
    int trigAngle = 0, trigAngleFire = -50;

    //TODO: initial pos changes depending on screen width height
    /* Idle position revolver bitmap transforms */
    public PointF revPosIdle = new PointF(-0.4f, 0.2f);

    /* Fire position revolver bitmap transforms */
    PointF revPosFire = new PointF(-0.07f, 0.04f);
    int revAngleFire =  90;
    float revScaleFire = 0.5f;

    public boolean hasFired = false;

    public float animStep = 0.0f;


    public Revolver(Point screen) {
        this.screen = screen;
        revPos = new Point((int) scaleX(revPosIdle.x),(int) scaleY(revPosIdle.y ));
    }


    /**
     * Animate to the beginning or end if interaction stops
     */
    public void touchUpAnim() {
        if(animStep <= 0f) {
            animStep = 0f;
        } else {
            if(animStep < 0.7f) {
                animStep -= 0.02f;
            } else {
                animStep = (animStep < 1f ? animStep + 0.02f: 1f);
            }
        }
    }

    /**
     * Set the transform for the revolver based on the % (animStep 0f - 1f)
     */
    public void stepFireAnimation() {

        revPos.x = (int) ((scaleX(revPosFire.x - revPosIdle.x) * animStep) + scaleX(revPosIdle.x));
        revPos.y = (int) ((scaleY(revPosFire.y - revPosIdle.y) * animStep) + scaleY(revPosIdle.y));

        revAngle = (int) (revAngleFire * animStep);
        revScale = (revScaleFire - 1) * animStep + 1;

        trigAngle = (int) (trigAngleFire * animStep);
    }


    /**
     * Apply the transforms for the trigger bitmap to be able to draw at 0,0
     * @param canvas canvas to apply the transforms to
     */
    public void transformTrigger(Canvas canvas) {

        transformRevolver(canvas);

        //Draw the revolver by applying the canvas rotated matrix.
        canvas.translate(scaleX(trigOffset.x), scaleY(trigOffset.y));
        //Rotate the canvas matrix.
        canvas.rotate(trigAngle, (scaleX(trigSize.x) / 2),  (scaleY(trigSize.y) / 2));
    }

    /**
     * Apply the transforms for the revolver bitmap to be able to draw at 0,0
     * @param canvas canvas to apply the transforms to
     */
    public void transformRevolver(Canvas canvas) {
        float centerX = revPos.x + (scaleX(revSize.x) / 2);
        float centerY = revPos.y + (scaleY(revSize.y) / 2);

        canvas.translate(revPos.x, revPos.y);
        canvas.scale(revScale, revScale, centerX, centerY);
        canvas.rotate(revAngle, centerX, centerY); //Rotate the canvas matrix.
    }




    // Util methods

    private float scaleX(float x) {
        return screen.x * x;
    }

    private float scaleY(float y) {
        return screen.y * y;
    }

}
