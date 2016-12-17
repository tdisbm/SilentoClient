package services.proxy.endpoints;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

class MessageWrapper {
    private String message;

    private List<String> proxyStack;

    public MessageWrapper() {
        this.proxyStack = new LinkedList<>();
    }

    public List<String> getProxyStack() {
        return proxyStack;
    }

    public MessageWrapper addProxyAddress(String address) {
        for (String p : this.proxyStack) {
            if (p.equals(address)) {
                return this;
            }
        }

        this.proxyStack.add(address);

        return this;
    }

    public MessageWrapper setProxyStack(List<String> proxyStack) {
        this.proxyStack = proxyStack;

        return this;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String obj) {
        this.message = obj;
    }

    public String charge() {
        return this.proxyStack.remove(0);
    }

    public String toString() {
        String proxyStack = String.format(
            "proxyStack: [\"%s\"]",
            String.join("\",\"", this.proxyStack)
        );

        return String.format("{message: %s, %s}", message, proxyStack);
    }

    public MessageWrapper parseString(String jsonString) {
        JSONArray proxyStack;

        try {
            JSONObject json = new JSONObject(jsonString);
            this.setMessage(json.get("message").toString());
            this.proxyStack.clear();

            proxyStack = (JSONArray) json.get("proxyStack");
            for (int i = 0, n = proxyStack.length(); i < n; i++) {
                this.addProxyAddress(proxyStack.get(i).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }
}
