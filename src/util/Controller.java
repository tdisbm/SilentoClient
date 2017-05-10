package util;

import javafx.scene.Scene;
import javafx.stage.Stage;
import kraken.unit.Container;

public abstract class Controller {
    private Scene scene;
    private Stage stage;
    private Configurator configurator;
    private Container container;

    public Controller() {
        configurator = new Configurator();
        configure(configurator);
    }

    public void setScene(Scene scene) {
        this.scene = scene;
    }

    public Scene getScene() {
        return scene;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public <T> T get(String id) {
        return container.get(id);
    }

    public abstract void configure(Configurator configurator);

    public Configurator getConfigurator() {
        return configurator;
    }


    public void onDisplay() {
        //OVERRIDE
    }

    public void onCreate() {
        //OVERRIDE
    }

    protected class Configurator {
        private String id;
        private String view;
        private boolean main;

        public String getId() {
            return id;
        }

        public Configurator setId(String id) {
            this.id = id;
            return this;
        }

        public String getView() {
            return view;
        }

        public Configurator setView(String view) {
            this.view = view;
            return this;
        }

        public boolean isMain() {
            return main;
        }

        public Configurator setMain(boolean main) {
            this.main = main;
            return this;
        }
    }
}
