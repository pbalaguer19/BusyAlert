package com.example.pau.busyalert.JavaClasses;

/**
 * Created by pau on 5/3/17.
 */

public class User {
    /**
     * This class is for UserAdapter.
     **/
    private String name;
    private String status;
    private String phone;

    public User(){
        this.name = "John Smith";
        this.status = "Busy";
    }

    public User(String name, String status){
        this.name = name;
        this.status = status;
    }

    public User(String name, String status, String phone){
        this.name = name;
        this.status = status;
        this.phone = phone;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
