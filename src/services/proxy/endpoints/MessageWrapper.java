package services.proxy.endpoints;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class MessageWrapper {
    private String message;
    private String status;
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
        try {
            return this.proxyStack.remove(0);
        } catch (IndexOutOfBoundsException e) {
            return  null;
        }
    }

    public String toString() {
        String proxyStack = String.format(
            "proxyStack: [\"%s\"]",
            String.join("\",\"", this.proxyStack)
        );

        return String.format("{message: %s, %s, status: %s}", message, proxyStack, status);
    }

    public MessageWrapper parseString(String jsonString) {
        JSONArray proxyStack;

        try {
            JSONObject json = new JSONObject(jsonString);
            this.setMessage(json.get("message").toString());
            this.setStatus(json.get("status").toString());
            this.proxyStack.clear();

            proxyStack = (JSONArray) json.get("proxyStack");
            for (int i = 0, n = proxyStack.length(); i < n; i++) {
                if (!proxyStack.get(i).toString().isEmpty())
                this.addProxyAddress(proxyStack.get(i).toString());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return this;
    }

    public String getStatus() {
        return status;
    }

    public MessageWrapper setStatus(String status) {
        this.status = status;
        return this;
    }
}
