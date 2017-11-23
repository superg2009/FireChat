package com.tru.firechat;


import java.util.Date;

/**
 * Created by super on 11/12/2017.
 */

@SuppressWarnings("DefaultFileTemplate")
public class Message {
    private String id;
    private String text;
    private String name;
    private String imageUrl;
    private String photoUrl;
    private long time;

    public Message(){}

    public Message(String text, String name, String imageUrl,String photoUrl) {
        this.text = text;
        this.name = name;
        this.imageUrl = imageUrl;
        time= new Date().getTime();
        this.photoUrl= photoUrl;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
