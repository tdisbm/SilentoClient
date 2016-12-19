//package services.proxy;
//
//import org.glassfish.tyrus.client.ClientManager;
//import org.glassfish.tyrus.server.Server;
//import services.proxy.endpoints.ProxyChain;
//import services.proxy.endpoints.ProxyTrigger;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.*;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ExecutionException;
//
//public class ProxyManager {
//    public static final int PROXY_DEEP = 3;
//
//    private List<String> proxyServersList;
//    private String extIp;
//    private PortScanner scanner;
//
//    private ProxyTrigger trigger;
//    private URI currentUri;
//    private ClientManager client;
//    private Server server;
//    private int deep;
//
//    public ProxyManager(int deep) {
//        this.deep = deep == 0 ? PROXY_DEEP : deep;
//        this.proxyServersList = new LinkedList<>();
//
//        detectExternalAddress();
//        initPortScanner();
//        runScanning();
//        createServer();
//    }
//
//    private void initPortScanner() {
//        if (extIp == null) {
//            return;
//        }
//
//        scanner = new PortScanner(extIp, null);
//    }
//
//    private void runScanning() {
//        if (scanner.getAvailablePorts().size() > 0) {
//            return;
//        }
//
//        new Thread(() -> {
//            try {
//                scanner.addCallback(() -> {
//                    createServer();
//                    scanner.clearCallbacks();
//                    return null;
//                });
//                scanner.runScanning();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }
//
//    private void createServer() {
//        List<Integer> available = scanner.getAvailablePorts();
//
//        if (available.size() == 0) {
//            runScanning();
//            return;
//        }
//
//        Socket socket;
//
//        for (int port : available) {
//            try {
//                try {
//                    socket = new Socket();
//                    socket.connect(new InetSocketAddress(extIp, port), 200);
//                } catch (SocketTimeoutException ignored) {
//                    trigger = new ProxyTrigger();
//                    trigger.setProxyManager(this);
//                    server = new Server("localhost", port, "/", ProxyChain.class);
//                    server.start();
//
//                    client = ClientManager.createClient();
//                    currentUri =  URI.create(String.format("ws://%s:%s%s", extIp, port, ProxyChain.ENDPOINT));
//                    client.connectToServer(trigger, currentUri);
//
//                    break;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//
//
//    public ProxyManager addProxyServer(String ip, int port) {
//        String address = String.format("ws://%s:%s%s", ip, port, ProxyChain.ENDPOINT);
//
//        for (String p : this.proxyServersList) {
//            if (p.equals(address)) {
//                return this;
//            }
//        }
//        this.proxyServersList.add(address);
//
//        return this;
//    }
//
//    public List<String> getProxyServersList() {
//        return proxyServersList;
//    }
//
//
//
//    public int getDeep() {
//        return deep;
//    }
//
//    public void setDeep(int deep) {
//        this.deep = deep;
//    }
//}
