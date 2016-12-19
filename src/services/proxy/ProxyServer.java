package services.proxy;


import services.event_subscriber.EventSubscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProxyServer {
    public static final String INVALID_SECURE_KEY = "invalid_secure_key";
    public static final String TERMINATE_EVENT = "terminate";

    private EventSubscriber eventSubscriber;
    private LinkedHashMap<Integer, ServerSocket> listeners;
    private ExecutorService serverExecutor;
    private String secureKey;

    public ProxyServer() {
        listeners = new LinkedHashMap<>();
        serverExecutor = Executors.newSingleThreadExecutor();
        eventSubscriber = new EventSubscriber();
    }

    public ProxyServer setSecureKey(String key) {
        this.secureKey = key;
        return this;
    }

    public String getSecureKey() {
        return this.secureKey;
    }

    public void listen(int port) {
        if (listeners.get(port) != null) {
            return;
        }

        try {
            final ServerSocket listener = new ServerSocket(port);

            serverExecutor.submit(() -> {
                while (!listeners.get(port).isClosed()) {
                    try {
                        Socket socket = listener.accept();
                        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

                        final String[] result = {""};

                        input.lines().forEach(s1 -> result[0] += s1);

                        ProxyTrigger pt = this.deserializeMessage(result[0]);

                        if (Objects.equals(pt.getSecureKey(), this.getSecureKey())) {
                            ProxyAddress pa = pt.charge();

                            do {
                                if (pa == null) {
                                    this.fireTerminateEvents(pt.getMessage());
                                    break;
                                }

                                Socket proxy = new Socket();
                                try {
                                    proxy.connect(new InetSocketAddress(pa.getAddress(), pa.getPort()), 200);
                                } catch (IOException ignored) {}

                                if (proxy.isConnected()) {
                                    PrintWriter pout = new PrintWriter(proxy.getOutputStream(), true);
                                    pout.println(pt.toJsonString());
                                    proxy.close();
                                    break;
                                }
                            } while ((pa = pt.charge()) != null);
                        } else {
                            out.println(INVALID_SECURE_KEY);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            this.listeners.put(port, listener);
        } catch (IOException ignored) {

        }
    }

    public boolean close(int port) {
        try {
            this.listeners.get(port).close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private ProxyTrigger deserializeMessage(String serialized) {
        ProxyTrigger pt = new ProxyTrigger(0);
        pt.parseJsonString(serialized);
        return pt;
    }

    public ProxyServer onTerminate(EventSubscriber.Callback<Object[]> tc) {
        this.eventSubscriber.subscribe(TERMINATE_EVENT, tc);
        return this;
    }

    public ProxyServer fireTerminateEvents(String message) {
        this.eventSubscriber.fire(TERMINATE_EVENT, message);
        return this;
    }
}
