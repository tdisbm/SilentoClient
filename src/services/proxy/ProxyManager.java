package services.proxy;

import com.fasterxml.jackson.databind.node.IntNode;
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
import java.util.concurrent.ExecutionException;

public class ProxyManager {
    private String extIp;
    private PortScanner scanner;
    private ProxyServer server;
    private ProxyTrigger trigger;
    private int scanAttempts = 2;

    public ProxyManager(IntNode deep) {
        this.extIp = detectExternalAddress();
        this.scanner = new PortScanner(extIp, null);
        this.trigger = new ProxyTrigger(deep.intValue());

        initScanner();
        createServer();
    }

    private String detectExternalAddress() {
        try {
            return new BufferedReader(
                    new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())
            ).readLine();
        } catch (IOException e) {
            return "";
        }
    }

    private void initScanner() {
        scanner.on(PortScanner.EVENT_SCAN_TERMINATE, (objects)->
            createServer()
        );
    }

    private void runScanning() {
        if (this.server.isListening(null)) {
            return;
        }

        if (scanAttempts == 0) {
            return;
        }

        scanAttempts--;

        javafx.application.Platform.runLater(() -> {
            try {
                scanner.runScanning();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private void createServer() {
        List<Integer> available = scanner.getAvailablePorts();

        if (available.size() == 0) {
            runScanning();
            return;
        }

        for (int port : available) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(extIp, port), 200);
            } catch (SocketTimeoutException e) {
                server = new ProxyServer();
                server.listen(port);
                server.fire(ProxyServer.EVENT_PROXY_SERVER_IS_UP, extIp, port);

                break;
            } catch (IOException ignored) {}
        }
    }

    public ProxyManager proxify(String jsonString) {
        try {
            trigger.prepareMessage(jsonString);
            ProxyAddress pa = trigger.charge();

            if (pa == null) {
                server.fire(ProxyServer.EVENT_PROXY_TERMINATE, jsonString);
                return this;
            }

            Socket s = new Socket();
            s.connect(new InetSocketAddress(pa.getAddress(), pa.getPort()), 200);
            PrintWriter out = new PrintWriter(s.getOutputStream(), true);
            out.println(trigger.toJsonString());
            s.close();
        } catch (Exception e) {
            server.fire(ProxyServer.EVENT_PROXY_TERMINATE, jsonString);
        }

        return this;
    }

    public ProxyManager setSecureKey(String secureKey) {
        this.server.setSecureKey(secureKey);
        this.trigger.setSecureKey(secureKey);
        return this;
    }

    public ProxyManager on(String event, EventSubscriber.Callback<Object[]> tc) {
        this.server.on(event, tc);
        return this;
    }

    public ProxyManager addProxyAddress(String host, int port) {
        this.trigger.addProxyAddress(host, port);
        return this;
    }
}
