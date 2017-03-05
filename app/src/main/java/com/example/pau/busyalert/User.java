package com.example.pau.busyalert;

/**
 * Created by pau on 5/3/17.
 */

public class User {
    private String name;
    private String status;

    public User(){
        this.name = "John Smith";
        this.status = "Busy";
    }

    public User(String name, String status){
        this.name = name;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
