package com.edaviessmith.sms_roulette.data;

import com.edaviessmith.sms_roulette.Var;

/**
 * Created by Ethan on 23/09/2015.
 */
public class InfoPhone extends Info {

    private int value;

    public InfoPhone(Var.Category category) {
        super(category);
    }

    public InfoPhone(Var.Category category, int value) {
        super(category);
        this.value = value;
    }

    /*@Override
    public Integer getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }*/
}
