package entity;

import org.json.JSONException;
import org.json.JSONObject;

public class Message {
    private String to;
    private String from;
    private String message;
    private String event;

    public String getTo() {
        return to;
    }

    public Message setTo(String to) {
        this.to = to;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Message setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Message setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getEvent() {
        return event;
    }

    public Message setEvent(String event) {
        this.event = event;
        return this;
    }

    public JSONObject toJsonObject() {
        JSONObject o = new JSONObject();
        try {
            o.put("message", message);
            o.put("from", from);
            o.put("to", to);
            o.put("event", event);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return o;
    }
}
