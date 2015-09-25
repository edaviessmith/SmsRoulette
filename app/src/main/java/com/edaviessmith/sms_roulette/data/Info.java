package com.edaviessmith.sms_roulette.data;

import com.edaviessmith.sms_roulette.Var;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Info {

    private Var.Category category; // Possibly an enum (phone, email, event)
    private int type;  // Work, Home, Mobile (sub category)
    private String value; // May change to object for future extensions

    public Info() {
    }

    public Info(Var.Category category) {
        this.category = category;
    }

    public Info(Var.Category category, int type, String value) {
        this.category = category;
        this.type = type;
        this.value = value;
    }

    public Var.Category getCategory() {
        return category;
    }

    public void setCategory(Var.Category category) {
        this.category = category;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
