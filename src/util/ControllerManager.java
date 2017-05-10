package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kraken.Kraken;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ControllerManager {
    private HashMap<String, Controller> controllers;
    private HashMap<String, Scene> loaded;
    private static ControllerManager instance;
    private Stage stage;

    private ControllerManager() {
        controllers = new HashMap<>();
        loaded = new HashMap<>();
    }

    public static ControllerManager getInstance() {
        if (instance == null) {
            instance = new ControllerManager();
        }

        return instance;
    }

    public ControllerManager addController(Controller controller) {
        this.controllers.put(controller.getConfigurator().getId(), controller);
        return this;
    }

    public void displayMain() {
        for (Map.Entry<String, Controller> controller : controllers.entrySet()) {
            if (controller.getValue().getConfigurator().isMain()) {
                changeController(controller.getKey());
            }
        }
    }

    public void changeController(String id) {
        Controller ctr = controllers.get(id);
        Controller.Configurator configurator = ctr.getConfigurator();

        if (loaded.get(id) != null) {
            stage.setScene(loaded.get(id));
            controllers.get(id).onDisplay();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(new File(configurator.getView()).toURI().toURL());
            Scene sc = new Scene(loader.load());
            Controller controller = loader.getController();

            if (controller == null) {
                System.out.println("Invalid controller");
                System.exit(1);
            }

            stage.setScene(sc);
            controller.setStage(stage);
            controller.setScene(sc);
            controller.setContainer(Kraken.getInstance().getContainer());
            controller.onCreate();

            controllers.put(id, controller);
            loaded.put(id, sc);

            stage.show();
            controller.onDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
