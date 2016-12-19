package services.proxy.components;


public interface JsonSerializable {
    public String toJsonString();
    public void parseJsonString(String json);
}
