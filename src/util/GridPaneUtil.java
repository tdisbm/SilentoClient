package util;

import javafx.scene.layout.GridPane;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GridPaneUtil {
    public static int countRows(GridPane pane) {
        Method method;
        try {
            method = pane.getClass().getDeclaredMethod("getNumberOfRows");
            method.setAccessible(true);
            return (Integer) method.invoke(pane);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            return -1;
        }
    }
}
