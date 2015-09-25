package com.edaviessmith.sms_roulette.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ethan on 23/09/2015.
 */
public class Contact {

    private String id;
    private String displayName;

    private List<Info> info;

    public Contact() {
        info = new ArrayList<>();
    }

    public List<Info> getInfo() {
        return info;
    }

    public void setInfo(List<Info> info) {
        this.info = info;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
