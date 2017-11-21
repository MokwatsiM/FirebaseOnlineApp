package com.example.mlab.androidpresencesystem.model;

/**
 * Created by mLab on 2017/11/08.
 */

public class User {
    private String email,status,userName;

    public User(String email, String status,String userName) {
        this.email = email;
        this.status = status;
        this.userName = userName;
    }

    public User() {

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
