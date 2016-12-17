

import entity.Message;
import services.proxy.ProxyManager;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        Message m = new Message();
        m.setSender("ion")
            .setReceiver("colea")
            .setMessage("noroc")
        ;

        ProxyManager pm = new ProxyManager(0);
        pm.addProxyServer("178.168.58.17", 1299);
        pm.addProxyServer("178.168.58.17", 6000);
        pm.addProxyServer("178.168.58.17", 7000);
        pm.proxify(m::toString);

        new Kraken()
            .sink(new File("resources/controllers.yml"))
            .sink(new File("resources/parameters.yml"))
            .sink(new File("resources/services.yml"))
        .dive();
    }
}
