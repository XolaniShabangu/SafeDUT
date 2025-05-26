package com.example.safedut;

public class ChatMessage {
    private String senderId;
    private String senderName;
    private String messageText;
    private long timestamp;

    public ChatMessage() {

    }




    public ChatMessage(String senderId, String senderName, String messageText, long timestamp) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
