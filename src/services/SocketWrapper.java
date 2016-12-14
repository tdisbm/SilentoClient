package services;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.socket.client.IO;
import io.socket.client.Socket;
import kraken.component.util.url.UrlBuilder;

import java.net.URISyntaxException;

public class SocketWrapper {
    Socket socket;

    public Socket connect(ObjectNode config) {
        if (this.socket != null) {
            this.socket.disconnect();
        }

        IO.Options opts = new IO.Options();
        UrlBuilder urlBuilder = new UrlBuilder(
            config.get("host").asText(),
            config.get("port").asText()
        );

        urlBuilder
            .addParameter("username", config.get("username").asText())
            .addParameter("role", config.get("role").asText())
        ;

        try {
            opts.query = urlBuilder.buildUrl(UrlBuilder.BUILD_QUERY);

            this.socket = IO.socket(urlBuilder.getHost(), opts);
            this.socket.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return this.socket;
    }

    public Socket getSocket() {
        return this.socket;
    }
}
