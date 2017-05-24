package com.example.pau.busyalert.JavaClasses;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by pau on 24/5/17.
 */

public class HerokuLog {
    @SerializedName("userId")
    @Expose
    String userId;

    @SerializedName("action")
    @Expose
    String action;

    @SerializedName("extraData")
    @Expose
    String extraData;


    public HerokuLog(String userId, String action, String extraData) {
        this.userId = userId;
        this.action = action;
        this.extraData = extraData;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getExtraData() {
        return extraData;
    }

    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }
}

