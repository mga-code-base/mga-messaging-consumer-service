package com.mygym.app.models;

public class BroadcastMessageDto {
    private String senderId;
    private String content;
    private String image;

    public BroadcastMessageDto() {}

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
