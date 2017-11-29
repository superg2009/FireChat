package com.tru.firechat;

@SuppressWarnings("DefaultFileTemplate")
public class Message {

    private String id;
    private String text;
    private String name;
    private String attachmentImageUrl;
    private String photoUrl;
    //default constructor
    public Message(){}

    public Message(String text, String name, String attachmentImageUrl, String photoUrl) {
        this.text = text;
        this.name = name;
        this.attachmentImageUrl = attachmentImageUrl;
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

    public String getAttachmentImageUrl() {
        return attachmentImageUrl;
    }

    public void setAttachmentImageUrl(String attachmentImageUrl) {
        this.attachmentImageUrl = attachmentImageUrl;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

}
