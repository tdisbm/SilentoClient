//package services.proxy.endpoints;
//
//import org.glassfish.tyrus.client.ClientManager;
//import org.json.JSONException;
//import org.json.JSONObject;
//import services.proxy.ProxyManager;
//
//import javax.websocket.*;
//import java.io.IOException;
//import java.net.URI;
//import java.util.*;
//import java.util.concurrent.*;
//
//@ClientEndpoint
//public class ProxyTrigger {
//    public static final int STATUS_EMPTY_MESSAGE = 2;
//    public static final int STATUS_MALFORMED_MESSAGE = 3;
//    public static final int STATUS_EMPTY_PROXY_LiST = 4;
//    public static final int STATUS_BROKEN = 5;
//    public static final int STATUS_PASSED = 6;
//    public static final int STATUS_DONE = 7;
//
//    private ProxyManager proxyManager;
//    private ClientManager client = ClientManager.createClient();
//    private int status;
//
//    private MessageWrapper messageWrapper;
//    private List<StatusChangeCallback> callbacks;
//    private ExecutorService executor;
//
//    private final static CountDownLatch latch = new CountDownLatch(1);
//
//    public ProxyTrigger() {
//        callbacks = new LinkedList<>();
//        messageWrapper = new MessageWrapper();
//        executor = Executors.newFixedThreadPool(1);
//    }
//
//    public ProxyTrigger setMessageWrapper(MessageWrapper mw) {
//        this.messageWrapper = mw;
//
//        return this;
//    }
//
//    @OnMessage
//    public void onMessage(String message, Session session) throws IOException, EncodeException {
//        if (Objects.equals(message, ProxyChain.TRANSMISSION_ALLOWED)) {
//            messageWrapper.setStatus(ProxyChain.TRANSMISSION_ALLOWED);
//            session.getBasicRemote().sendText(messageWrapper.toString());
//            return;
//        }
//
//        this.messageWrapper.parseString(message);
//
//        if (Objects.equals(this.messageWrapper.getStatus(), ProxyChain.TRANSMISSION_PASSED)) {
//            final String nextUri[] = new String[1];
//            Future<Boolean> f;
//            while (true) {
//                nextUri[0] = messageWrapper.charge();
//                f = executor.submit(() -> {
//                    client.connectToServer(this, URI.create(nextUri[0]));
//                    return true;
//                });
//
//                try {
//                    boolean connected = f.get(6000, TimeUnit.MILLISECONDS);
//                    if (connected) {
//                        updateStatus(STATUS_PASSED);
//                        return;
//                    }
//                } catch (InterruptedException | ExecutionException | TimeoutException e) {
//                    if (nextUri[0] == null) {
//                        updateStatus(STATUS_DONE);
//                        return;
//                    }
//                }
//            }
//        }
//
//        latch.countDown();
//    }
//
//    @OnClose
//    public void onClose(Session session, CloseReason closeReason) throws IOException {
//        if (Objects.equals(closeReason.getReasonPhrase(), ProxyChain.TRANSMISSION_PASSED)) {
//            updateStatus(STATUS_PASSED);
//        } else {
//            updateStatus(STATUS_BROKEN);
//        }
//        latch.countDown();
//    }
//
//    public void updateStatus(int status) {
//        this.status = status;
//
//        for (StatusChangeCallback c : callbacks) {
//            c.call(this.status);
//        }
//    }
//
//    public void setProxyManager(ProxyManager proxyManager) {
//        this.proxyManager = proxyManager;
//    }
//
//    public ProxyTrigger prepareMessage(String message) {
//        if (message == null) {
//            updateStatus(STATUS_EMPTY_MESSAGE);
//            return this;
//        }
//
//        try {
//            new JSONObject(message);
//        } catch (JSONException e) {
//            status = STATUS_MALFORMED_MESSAGE;
//            System.out.println("Proxy message must be valid json string");
//            return this;
//        }
//
//        messageWrapper.setMessage(message);
//        messageWrapper.getProxyStack().clear();
//        List<String> proxyServers = proxyManager.getProxyServersList();
//
//
//        int deep = proxyManager.getDeep();
//        int min = 0;
//        int max = proxyServers.size();
//
//        if (max == 0) {
//            status = STATUS_EMPTY_PROXY_LiST;
//            return this;
//        }
//
//        ArrayList<Integer> list = new ArrayList<>();
//        for (int i = min; i < max; i++) {
//            list.add(i);
//        }
//
//        if (deep > max) {
//            deep = max;
//        }
//
//        Collections.shuffle(list);
//        for (int i = min; i < deep; i++) {
//            messageWrapper.addProxyAddress(proxyServers.get(list.get(i)));
//        }
//
//        return this;
//    }
//
//    public ProxyTrigger onStatusChange(StatusChangeCallback c) {
//        callbacks.add(c);
//
//        return this;
//    }
//
//    @FunctionalInterface
//    public interface StatusChangeCallback {
//        void call(int status);
//    }
//}
