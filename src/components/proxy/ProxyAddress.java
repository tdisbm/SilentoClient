package components.proxy;

public class ProxyAddress {
    private String address;
    private int port;

    public ProxyAddress(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
