package entity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;

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
            for (Field f : getClass().getDeclaredFields()) {
                f.setAccessible(true);
                o.put(f.getName(), f.get(this));
            }
        } catch (JSONException | IllegalAccessException ignored) {
        }

        return o;
    }
}
