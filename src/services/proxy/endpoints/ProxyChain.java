package services.proxy.endpoints;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

@ServerEndpoint(ProxyChain.ENDPOINT)
public class ProxyChain {
    public static final String ENDPOINT = "/next";
    public static final String MESSAGE_RECEIVED = "received";

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        session.getBasicRemote().sendObject(MESSAGE_RECEIVED);
    }

    @OnMessage
    public String onMessage(String message, Session session) {
        System.out.println(message);
        return "";
    }
}
