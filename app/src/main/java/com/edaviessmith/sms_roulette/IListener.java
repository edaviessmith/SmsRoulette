package com.edaviessmith.sms_roulette;

/**
 * Created by Ethan on 17/10/2015.
 */
public interface IListener {

    void onProgress(float percent);
    void onComplete();
    void onComplete(String value);
    void onError(String value);
}

