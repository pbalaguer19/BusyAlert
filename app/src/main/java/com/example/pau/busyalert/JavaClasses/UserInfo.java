package com.example.pau.busyalert.JavaClasses;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pau on 27/3/17.
 */

public class UserInfo {
    private String username;
    private String email;
    private String status;
    private List<UserInfo> friends;
    private List<UserInfo> favouriteFriends;

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

    public String getStatus() {
        return status;
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

    public void setStatus(String status) {
        this.status = status;
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
        public static final String BUSY = "Busy";
        public static final String AVAILABLE = "Available";
    }
}
