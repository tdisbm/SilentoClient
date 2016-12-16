package services.proxy.endpoints;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(ProxyChain.ENDPOINT)
public class ProxyChain {
    public static final String ENDPOINT = "/next";

    @OnOpen
    public void onOpen(Session session) throws IOException {
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        return "";
    }

    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
    }
}
