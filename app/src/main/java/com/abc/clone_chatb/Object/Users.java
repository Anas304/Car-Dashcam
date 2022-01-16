package com.abc.clone_chatb.Object;

public class Users {
    private String id,username,imageURL,status;

    public Users(){}

    public Users(String id, String imageURL, String username, String status) {
        this.id = id;
        this.imageURL = imageURL;
        this.username = username;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getUsername() {
        return username;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
