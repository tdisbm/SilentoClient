package services.proxy.endpoints;

import services.proxy.ProxyManager;

import javax.websocket.*;
import java.io.IOException;

@ClientEndpoint
public class ProxyTrigger {
    private ProxyManager proxyManager;
    private String currentMessage;

    @OnOpen
    public void onOpen(Session session) throws IOException {
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        return "";
    }

    public void setProxyManager(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }

    public ProxyTrigger prepareMessage(String message) {
        currentMessage = message;

        return this;
    }

    private String serializeMessage() {
        return null;
    }
}
