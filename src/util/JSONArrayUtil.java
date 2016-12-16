package util;

import org.json.JSONArray;
import org.json.JSONException;

public class JSONArrayUtil {
    public static int indexOf(JSONArray j, String e) {
        try {
            for(int i = 0, n = j.length(); i < n; i++) {
                if(e.equals(j.get(i))) {
                    return i;
                }
            }
        } catch (JSONException e1) {
            return -1;
        }

        return -1;
    }
}
