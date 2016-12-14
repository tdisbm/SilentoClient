package services;

import io.socket.client.IO;
import io.socket.client.Socket;

import java.net.URISyntaxException;

public class SocketWrapper {
    Socket socket;

    public Socket connect(String url, IO.Options opts) {
        if (this.socket != null) {
            this.socket.disconnect();
        }

        try {
            this.socket = IO.socket(url, opts);
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
