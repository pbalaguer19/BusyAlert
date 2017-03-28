package com.example.pau.busyalert.JavaClasses;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pau on 27/3/17.
 */

public class UserInfo {
    public String username;
    public String email;
    public String status;
    public String phone;

    public UserInfo(String username, String email, String phone) {
        this.username = username;
        this.email = email;
        this.status = Status.AVAILABLE;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStatus() {
        return status;
    }

    public boolean isStatusBusy(){
        return this.status.equals(Status.BUSY);
    }

    public boolean isStatusAvailable(){
        return this.status.equals(Status.AVAILABLE);
    }

    public void setStatusToBusy(){
        this.status = Status.BUSY;
    }

    public void setStatusToAvailable(){
        this.status = Status.AVAILABLE;
    }

    public String getPhone() {
        return phone;
    }

    private class Status{
        private static final String BUSY = "Busy";
        private static final String AVAILABLE = "Available";
    }
}
