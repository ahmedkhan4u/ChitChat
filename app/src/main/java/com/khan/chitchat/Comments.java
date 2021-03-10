package com.khan.chitchat;

public class Comments {
    String comment,date,username;

    public Comments(){

    }

    public Comments(String comment, String date, String username) {
        this.comment = comment;
        this.date = date;
        this.username = username;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
