package entity;

import java.util.List;

public class Message {
    private String text;
    private String receiver;
    private String sender;
    private List<String> proxyStack;


    public String getText() {
        return text;
    }

    public Message setText(String text) {
        this.text = text;

        return this;
    }

    public String getReceiver() {
        return receiver;
    }

    public Message setReceiver(String receiver) {
        this.receiver = receiver;

        return this;
    }

    public String getSender() {
        return sender;
    }

    public Message setSender(String sender) {
        this.sender = sender;

        return this;
    }
}
