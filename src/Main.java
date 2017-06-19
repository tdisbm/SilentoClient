import components.ParameterExtension;
import controller.ChatController;
import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import kraken.Kraken;
import util.ControllerManager;

import java.io.File;

public class Main extends Application {
    public static void main(String[] args) {
        Kraken.getInstance()
            .sink(new File("resources/parameters.yml"))
            .sink(new File("resources/services.yml"))
            .sink(new File("resources/tasks.yml"))
            .sink(new ParameterExtension())
        .dive();

        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        ControllerManager controllerManager = ControllerManager.getInstance();
        controllerManager.setStage(stage);
        controllerManager
            .addController(new LoginController())
            .addController(new ChatController())
        ;

        controllerManager.displayMain();
    }
}
