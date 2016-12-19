package services.proxy;

import services.event_subscriber.EventSubscriber;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class ProxyManager {
    private String extIp;
    private PortScanner scanner;
    private ProxyServer server;
    private ProxyTrigger trigger;

    public ProxyManager(int deep) {
        this.extIp = detectExternalAddress();
        this.scanner = new PortScanner(extIp, null);
        this.trigger = new ProxyTrigger(deep);

        createServer();
    }

    public String detectExternalAddress() {
        try {
            return new BufferedReader(
                    new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())
            ).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void runScanning() {
        if (scanner.getAvailablePorts().size() > 0) {
            return;
        }

        new Thread(() -> {
            try {
                scanner.addCallback(() -> {
                    createServer();
                    scanner.clearCallbacks();
                    return null;
                });
                scanner.runScanning();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void createServer() {
        List<Integer> available = scanner.getAvailablePorts();

        if (available.size() == 0) {
            runScanning();
            return;
        }

        Socket socket;

        for (int port : available) {
            try {
                try {
                    socket = new Socket();
                    socket.connect(new InetSocketAddress(extIp, port), 200);
                } catch (SocketTimeoutException ignored) {
                    server = new ProxyServer();
                    server.listen(port);

                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public ProxyManager proxify(String jsonString) {
        if (server == null) {
            return this;
        }

        try {
            trigger.prepareMessage(jsonString);
            ProxyAddress pa = trigger.charge();

            if (pa == null) {
                server.fireTerminateEvents(jsonString);
                return this;
            }

            Socket s = new Socket();
            s.connect(new InetSocketAddress(pa.getAddress(), pa.getPort()), 200);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(trigger.toJsonString());
        } catch (Exception e) {
            server.fireTerminateEvents(jsonString);
        }

        return this;
    }

    public ProxyManager setSecureKey(String secureKey) {
        this.server.setSecureKey(secureKey);
        this.trigger.setSecureKey(secureKey);
        return this;
    }

    public ProxyManager onTerminate(EventSubscriber.Callback<Object[]> tc) {
        this.server.onTerminate(tc);
        return this;
    }

    public ProxyManager addProxyAddress(String host, int port) {
        this.trigger.addProxyAddress(host, port);
        return this;
    }
}
