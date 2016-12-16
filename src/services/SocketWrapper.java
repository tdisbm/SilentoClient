package services;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class SocketWrapper {
    private IO.Options opts;
    private Socket socket;
    private String url;

    public void initialize(String url, IO.Options opts) {
        this.opts = opts;
        this.url = url;
    }

    public Socket create() {
        if (socket != null) {
            socket.disconnect();
        }

        if (url == null) {
            return null;
        }

        try {
            socket = IO.socket(url, opts);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return socket;
    }

    public void connect() {
        if (socket != null) {
            socket.connect();
        }
    }

    public IO.Options getOpts() {
        return this.opts;
    }

    public Socket getSocket() {
        return this.socket;
    }
}
