package entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private String message;
    private String receiver;
    private String sender;

    public String getMessage() {
        return message;
    }

    public Message setMessage(String message) {
        this.message = message;

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

    public String toString() {
        return String.format("{" +
            "sender: \"%s\"," +
            "receiver: \"%s\"," +
            "message: \"%s\"" +
            "}",
            sender,
            receiver,
            message
        );
    }

    public Message parseString(String toParse) {
        try {
            JSONObject json = new JSONObject(toParse);
            sender = (String) json.get("sender");
            receiver = (String) json.get("receiver");
            message = (String) json.get("message");
        } catch (JSONException ignored) {
        }

        return this;
    }

    public static Message fromString(String jsonString) {
        Message instance = new Message();
        try {
            JSONObject json = new JSONObject(jsonString);
            instance.setSender((String) json.get("sender"));
            instance.setReceiver((String) json.get("receiver"));
            instance.setMessage((String) json.get("message"));
        } catch (JSONException ignored) {
        }

        return instance;
    }
}
