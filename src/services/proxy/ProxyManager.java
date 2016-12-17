package services.proxy;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import services.proxy.endpoints.ProxyChain;
import services.proxy.endpoints.ProxyTrigger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class ProxyManager {
    public static final int PROXY_DEEP = 3;

    private List<String> proxyServersList;
    private String externalAddress;
    private PortScanner scanner;

    private ProxyTrigger sle;
    private ClientManager trigger;
    private Server server;
    private int usedPort;
    private int deep;

    public ProxyManager(int deep) {
        this.deep = deep == 0 ? PROXY_DEEP : deep;
        this.proxyServersList = new LinkedList<>();

        detectExternalAddress();
        initPortScanner();
        runScanning();
        createServer();
    }

    private void initPortScanner() {
        if (externalAddress == null) {
            return;
        }

        scanner = new PortScanner(externalAddress, null);
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

        Server server;
        Socket socket;

        for (int port : available) {
            try {
                server = new Server("localhost", port, "/", ProxyChain.class);
                server.start();

                socket = new Socket();
                socket.connect(new InetSocketAddress(externalAddress, port), 200);

                if (socket.isConnected()) {
                    this.sle = new ProxyTrigger();
                    this.sle.setProxyManager(this);
                    this.server = server;
                    this.trigger = ClientManager.createClient();
                    this.usedPort = port;

                    socket.close();
                    break;
                }

                socket.close();
                server.stop();
            } catch (Exception ignored) {}
        }
    }

    private void detectExternalAddress() {
        try {
            externalAddress = new BufferedReader(
                new InputStreamReader(new URL("http://checkip.amazonaws.com").openStream())
            ).readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ProxyManager addProxyServer(String ip, int port) {
        String address = String.format("ws://%s:%s%s", ip, port, ProxyChain.ENDPOINT);

        for (String p : this.proxyServersList) {
            if (p.equals(address)) {
                return this;
            }
        }
        this.proxyServersList.add(address);

        return this;
    }

    public List<String> getProxyServersList() {
        return proxyServersList;
    }

    public ProxyManager proxify(Callable<Object> callable) {
        try {
            this.sle.prepareMessage(callable.call());
            this.trigger.connectToServer(sle, URI.create(String.format(
                "ws://%s:%s%s",
                externalAddress,
                usedPort,
                ProxyChain.ENDPOINT
            )));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }
}
