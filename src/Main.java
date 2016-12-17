

import entity.Message;
import services.proxy.ProxyManager;

import java.util.concurrent.Callable;

public class Main {
    public static void main(String[] args) {
        ProxyManager pm = new ProxyManager(0);

        pm.proxify(() -> {
            Message m = new Message();
            m.setReceiver("ion")
                .setReceiver("colea")
                .setText("noroc")
            ;

            return m;
        });

//        new Kraken()
//            .sink(new File("resources/controllers.yml"))
//            .sink(new File("resources/parameters.yml"))
//            .sink(new File("resources/services.yml"))
//        .dive();
    }
}
