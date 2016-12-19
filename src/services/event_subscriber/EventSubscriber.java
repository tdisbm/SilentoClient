package services.event_subscriber;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class EventSubscriber {
    private LinkedHashMap<String, List<Callback<Object[]>>> events;

    public EventSubscriber() {
        this.events = new LinkedHashMap<>();
    }

    public EventSubscriber subscribe(String name, Callback<Object[]> callable) {
        List<Callback<Object[]>> c = this.events.get(name);

        if (c == null) {
            c = new LinkedList<>();
        }

        c.add(callable);
        this.events.put(name, c);

        return this;
    }

    public EventSubscriber fire(String name, Object... args) {
        List<Callback<Object[]>> c = this.events.get(name);

        if (c != null) {
            c.forEach(callable -> {
                try {
                    callable.call(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        return this;
    }

    public EventSubscriber close(String name) {
        this.events.remove(name);
        return this;
    }

    @FunctionalInterface
    public interface Callback<T> {
        void call(T result);
    }
}
