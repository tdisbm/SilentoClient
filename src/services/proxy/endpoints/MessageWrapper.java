package services.proxy.endpoints;

import java.util.LinkedList;
import java.util.List;

class MessageWrapper {
    private Object obj;

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

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
