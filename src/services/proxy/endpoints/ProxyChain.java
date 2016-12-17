package services.proxy.endpoints;

import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

@ServerEndpoint(ProxyChain.ENDPOINT)
public class ProxyChain {
    public static final String ENDPOINT = "/next";
    public static final String TRANSMISSION_INITIALIZED = "initialized";
    public static final String TRANSMISSION_PASSED = "passed";
    public static final String TRANSMISSION_DONE = "done";

    private MessageWrapper mw = new MessageWrapper();
    private CountDownLatch latch = new CountDownLatch(1);
    private ClientManager client = ClientManager.createClient();

    @OnOpen
    public void onOpen(Session session) throws IOException, EncodeException {
        session.getBasicRemote().sendObject(TRANSMISSION_INITIALIZED);
        latch.countDown();
    }

    @OnMessage
    public void onMessage(String message, Session session) throws IOException, EncodeException, DeploymentException {
        mw.parseString(message);
        ProxyTrigger pt = new ProxyTrigger();
        pt.setMessageWrapper(mw);

        client.connectToServer(pt, URI.create(mw.charge()));
        session.close(new CloseReason(
            CloseReason.CloseCodes.NORMAL_CLOSURE,
            TRANSMISSION_PASSED
        ));

        latch.countDown();
    }
}
