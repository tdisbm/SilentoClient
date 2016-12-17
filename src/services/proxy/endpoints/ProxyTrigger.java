package services.proxy.endpoints;

import com.sun.xml.internal.ws.api.message.MessageWritable;
import services.proxy.ProxyManager;

import javax.websocket.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@ClientEndpoint
public class ProxyTrigger {
    public static final int STATUS_SENT = 1;
    public static final int STATUS_PROCESSING = 3;
    public static final int STATUS_INVALID_MESSAGE = 3;
    public static final int STATUS_EMPTY_PROXY_LiST = 4;

    private ProxyManager proxyManager;
    private int status;

    private MessageWrapper mw;

    public ProxyTrigger() {
        mw = new MessageWrapper();
    }

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        if (mw.getObj() == null) {
            status = STATUS_INVALID_MESSAGE;
            return;
        }
        status = STATUS_PROCESSING;
        session.getBasicRemote().sendObject(mw);
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        if (Objects.equals(message, ProxyChain.MESSAGE_RECEIVED)) {
            status = STATUS_SENT;
        }

        return null;
    }

    public void setProxyManager(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    public ProxyTrigger prepareMessage(Object message) {
        if (message == null) {
            return this;
        }

        mw.setObj(message);
        List<String> proxyServers = proxyManager.getProxyServersList();


        int deep = proxyManager.getDeep();
        int min = 0;
        int max = proxyServers.size() + 1;

        if (max == 1) {
            status = STATUS_EMPTY_PROXY_LiST;
            return this;
        }

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = min; i < max; i++) {
            list.add(i);
        }

        Collections.shuffle(list);
        for (int i = min; i < deep; i++) {
            mw.addProxyAddress(proxyServers.get(list.get(i)));
        }

        return this;
    }

    public int getStatus() {
        return status;
    }
}
