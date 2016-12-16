package util;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONObjectUtil {
    public static <T, K> T get(String id, K o) {
        try {
            return (T) ((JSONObject) o).get(id);
        } catch (ClassCastException | JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONObject put(String id, JSONObject j, Object what) {
        try {
            j.put(id, what);
        } catch (JSONException ignored) {}

        return j;
    }
}
