package com.example.mlab.androidpresencesystem.model;

/**
 * Created by mLab on 2017/11/16.
 */

public class Tracking {
    private String email,uid,lat,lng,userName;

    public Tracking() {
    }

    public Tracking(String email, String uid, String lat, String lng,String userName) {
        this.email = email;
        this.uid = uid;
        this.lat = lat;
        this.lng = lng;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
