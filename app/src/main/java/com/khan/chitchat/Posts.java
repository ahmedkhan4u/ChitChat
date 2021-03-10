package com.khan.chitchat;

public class Posts {

    public String uid, date, description, fullname, postimage, profileimage;

    public Posts()
    {

    }

    public Posts(String uid, String date, String description, String fullname, String postimage, String profileimage) {
        this.uid = uid;
        this.date = date;
        this.description = description;
        this.fullname = fullname;
        this.postimage = postimage;
        this.profileimage = profileimage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }
}
