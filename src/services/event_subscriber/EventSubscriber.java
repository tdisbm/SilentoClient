package services.event_subscriber;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventSubscriber {
    private LinkedHashMap<String, List<Callback<Object[]>>> events;
    private ExecutorService executor;

    public EventSubscriber() {
        this.events = new LinkedHashMap<>();
        this.executor = Executors.newFixedThreadPool(20);
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
            try {
                c.forEach(callable -> {
                    executor.submit(() -> {
                        callable.call(args);

                        return null;
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this;
    }

    public EventSubscriber unsubscribe(String name) {
        this.events.remove(name);
        return this;
    }

    @FunctionalInterface
    public interface Callback<T> {
        void call(T result);
    }
}
