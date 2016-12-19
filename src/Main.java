

import entity.Message;
import services.proxy.experimental.ProxyManager;
import services.proxy.experimental.ProxyServer;

import java.io.*;

public class Main {
    public static void main(String[] args) {
        Message m = new Message();
        m.setSender("ion")
            .setReceiver("colea")
            .setMessage("noroc")
        ;

        ProxyManager pm = new ProxyManager(0);
        pm.setSecureKey("tester");
        pm.onTerminate(result -> {
            System.out.println(result);
        });

        pm.addProxyAddress("178.168.58.17", 1300);
        pm.proxify(m.toString());

        new Kraken()
            .sink(new File("resources/controllers.yml"))
            .sink(new File("resources/parameters.yml"))
            .sink(new File("resources/services.yml"))
        .dive();
    }
}
