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
    public List<UserInfo> friends;
    public List<UserInfo> favouriteFriends;

    public UserInfo(String username, String email) {
        this.username = username;
        this.email = email;
        this.status = Status.AVAILABLE;
        this.friends = new ArrayList<>();
        this.favouriteFriends = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public List<UserInfo> getFriends() {
        return friends;
    }

    public List<UserInfo> getFavouriteFriends() {
        return favouriteFriends;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public List<UserInfo> addFavouriteFriend(UserInfo userInfo){
        this.favouriteFriends.add(userInfo);
        return this.favouriteFriends;
    }

    public List<UserInfo> addFriend(UserInfo userInfo){
        this.friends.add(userInfo);
        return this.friends;
    }

    private class Status{
        private static final String BUSY = "Busy";
        private static final String AVAILABLE = "Available";
    }
}
