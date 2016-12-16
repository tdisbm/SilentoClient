package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class FileUtil {
    public static boolean createFileIfAbsent(String path) {
        File f = new File(path);
        try {
            if (!f.exists()) {
                f.getParentFile().mkdirs();
                return f.createNewFile();
            }
        } catch (IOException e) {
            return false;
        }

        return false;
    }

    public static boolean checkFile(String filepath) {
        return filepath != null && new File(filepath).exists();
    }

    public static String absolute(String path) {
        return Paths.get("").toAbsolutePath().toString() + File.separator + path;
    }
}
