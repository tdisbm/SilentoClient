package services.proxy.endpoints;

import org.json.JSONException;
import org.json.JSONObject;
import services.proxy.ProxyManager;

import javax.websocket.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class ProxyTrigger {
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_EMPTY_MESSAGE = 2;
    public static final int STATUS_MALFORMED_MESSAGE = 3;
    public static final int STATUS_EMPTY_PROXY_LiST = 4;
    public static final int STATUS_BROKEN = 5;
    public static final int STATUS_PASSED = 6;

    private ProxyManager proxyManager;
    private int status;

    private MessageWrapper mw;
    private CountDownLatch latch = new CountDownLatch(1);
    private List<StatusChangeCallback> callbacks;

    public ProxyTrigger() {
        callbacks = new LinkedList<>();
        mw = new MessageWrapper();
    }

    public ProxyTrigger setMessageWrapper(MessageWrapper mw) {
        this.mw = mw;

        return this;
    }

    @OnMessage
    public void onMessage(String transmissionStatus, Session session) throws IOException, EncodeException {
        if (Objects.equals(transmissionStatus, ProxyChain.TRANSMISSION_INITIALIZED)) {
            if (mw.getMessage() == null) {
                updateStatus(STATUS_EMPTY_MESSAGE);
                return;
            }
            updateStatus(STATUS_PROCESSING);
            session.getBasicRemote().sendText(mw.toString());
        }

        latch.countDown();
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) throws IOException {
        if (Objects.equals(closeReason.getReasonPhrase(), ProxyChain.TRANSMISSION_PASSED)) {
            updateStatus(STATUS_PASSED);
        } else {
            updateStatus(STATUS_BROKEN);
        }
    }

    private void updateStatus(int status) {
        this.status = status;

        for (StatusChangeCallback c : callbacks) {
            c.call(this.status);
        }
    }

    public void setProxyManager(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    public ProxyTrigger prepareMessage(String message) {
        if (message == null) {
            updateStatus(STATUS_EMPTY_MESSAGE);
            return this;
        }

        try {
            new JSONObject(message);
        } catch (JSONException e) {
            status = STATUS_MALFORMED_MESSAGE;
            System.out.println("Proxy message must be valid json string");
            return this;
        }

        mw.setMessage(message);
        List<String> proxyServers = proxyManager.getProxyServersList();


        int deep = proxyManager.getDeep();
        int min = 0;
        int max = proxyServers.size();

        if (max == 0) {
            status = STATUS_EMPTY_PROXY_LiST;
            return this;
        }

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = min; i < max; i++) {
            list.add(i);
        }

        if (deep > max) {
            deep = max;
        }

        Collections.shuffle(list);
        for (int i = min; i < deep; i++) {
            mw.addProxyAddress(proxyServers.get(list.get(i)));
        }

        return this;
    }

    public ProxyTrigger onStatusChange(StatusChangeCallback c) {
        callbacks.add(c);

        return this;
    }

    @FunctionalInterface
    public interface StatusChangeCallback {
        void call(int status);
    }
}
