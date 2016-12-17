package services.proxy.endpoints;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

@ServerEndpoint(ProxyChain.ENDPOINT)
public class ProxyChain {
    public static final String ENDPOINT_SECRET = "wh49s5lb2ne1";
    public static final String ENDPOINT = "/next/" + ENDPOINT_SECRET;
    public static final String TRANSMISSION_PASSED = "passed";
    public static final String TRANSMISSION_ALLOWED = "allowed";

    private static final List<Session> peers = new LinkedList<>();
    private static final CountDownLatch latch = new CountDownLatch(1);
    private MessageWrapper messageWrapper = new MessageWrapper();

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        if (!peers.isEmpty()) {
            session.getBasicRemote().sendText(TRANSMISSION_ALLOWED);
            return;
        }

        peers.add(session);
        latch.countDown();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, EncodeException, DeploymentException {
        messageWrapper.parseString(message);

        if (Objects.equals(messageWrapper.getStatus(), TRANSMISSION_ALLOWED)) {
            messageWrapper.setStatus(TRANSMISSION_PASSED);
            peers.get(0).getBasicRemote().sendText(messageWrapper.toString());
        }
        latch.countDown();
    }
}
