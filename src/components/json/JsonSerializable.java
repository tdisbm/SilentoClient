package components.json;


public interface JsonSerializable {
    String toJsonString();
    void parseJsonString(String json);
}
