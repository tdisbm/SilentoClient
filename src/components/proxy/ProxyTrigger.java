package components.proxy;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import components.json.JsonSerializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ProxyTrigger implements JsonSerializable {
    private static final int DEFAULT_PROXY_DEEP = 2;
    private String message;
    private List<ProxyAddress> proxyStack;
    private List<ProxyAddress> proxyRoute;
    private String secureKey;

    private int deep;

    public ProxyTrigger(int deep) {
        this.deep = deep != 0 ? deep : DEFAULT_PROXY_DEEP;
        this.proxyRoute = new LinkedList<>();
        this.proxyStack = new LinkedList<>();
    }

    public ProxyTrigger setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMessage() {
        return this.message;
    }

    public ProxyTrigger setSecureKey(String secureKey) {
        this.secureKey = secureKey;
        return this;
    }

    public String getSecureKey() {
        return secureKey;
    }

    public ProxyTrigger addProxyAddress(String address, int port) {
        for (ProxyAddress p : this.proxyStack) {
            if (p.getAddress().equals(address) && p.getPort() == port) {
                return this;
            }
        }

        this.proxyStack.add(new ProxyAddress(address, port));

        return this;
    }

    public ProxyAddress charge() {
        try {
            return this.proxyRoute.remove(0);
        } catch (IndexOutOfBoundsException e) {
            return  null;
        }
    }

    public String toJsonString() {
        StringBuilder serializedProxyStack = new StringBuilder();
        ProxyAddress current;
        for (int i = 0, n = proxyRoute.size(); i < n; i++) {
            current = proxyRoute.get(i);

            if (current != null) {
                serializedProxyStack.append(proxyRoute.get(i).getAddress()).append(":").append(current.getPort());
                if (i != n - 1) {
                    serializedProxyStack.append(", ");
                }
            }
        }

        return String.format(
            "{proxyRoute: [\"%s\"], message: %s, secureKey: \"%s\"}",
                serializedProxyStack.toString(),
            message,
            secureKey
        );
    }

    public void parseJsonString(String jsonString) {
        JSONArray proxyStack;

        try {
            JSONObject json = new JSONObject(jsonString);
            this.setMessage(json.get("message").toString());
            this.proxyRoute.clear();

            proxyStack = (JSONArray) json.get("proxyRoute");
            String[] route;
            for (int i = 0, n = proxyStack.length(); i < n; i++) {
                route = proxyStack.get(i).toString().split(":");
                if (!proxyStack.get(i).toString().isEmpty()) {
                    this.proxyRoute.add(new ProxyAddress(route[0], Integer.parseInt(route[1])));
                }
            }

            this.setSecureKey((String) json.get("secureKey"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ProxyTrigger prepareMessage(String message) {
        if (message == null) {
            return this;
        }

        try {
            new JSONObject(message);
        } catch (JSONException e) {
            System.out.println("Proxy message must be valid json string");
            return this;
        }

        this.setMessage(message);

        int deep = this.deep;
        int min = 0;
        int max = proxyStack.size();

        if (max == 0) {
            return this;
        }

        proxyRoute.clear();

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = min; i < max; i++) {
            list.add(i);
        }

        if (deep > max) {
            deep = max;
        }

        Collections.shuffle(list);
        for (int i = 0; i < deep; i++) {
            proxyRoute.add(proxyStack.get(list.get(i)));
        }

        return this;
    }
}
