package controller;

import com.fasterxml.jackson.databind.node.TextNode;
import entity.User;
import io.socket.client.IO;
import io.socket.client.Socket;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import kraken.Kraken;
import kraken.unit.Container;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.SocketRoles;
import services.SocketWrapper;
import services.proxy.ProxyManager;
import util.*;

public class LoginController extends Controller {
    public final static String ID = "login_controller";
    public static final String VIEW = "resources/views/login.fxml";
    public final static String MESSAGE_MANDATORY_USERNAME = "username is mandatory";

    public TextField username;
    public Text error;

    private SocketWrapper socketWrapper;
    private ProxyManager proxyManager;
    private User user;

    public LoginController() {
        Container container = Kraken.getInstance().getContainer();
        TextNode url = container.get("parameters.silento_server_url");

        proxyManager = container.get("services.proxy_manager");
        socketWrapper = container.get("services.socket_wrapper");
        socketWrapper.initialize(url.asText(), new IO.Options());
        user = container.get("services.user_entity");
    }

    @Override
    public void onDisplay() {
        username.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= Constraints.USERNAME_MAX_LENGTH) {
                ((StringProperty)observable).setValue(oldValue);
            }
        });
    }

    public void loginAction() {
        String username = this.username.getText();

        if (username.isEmpty()) {
            this.showErrorMessage(MESSAGE_MANDATORY_USERNAME);
            return;
        }

        socketWrapper.getOpts().query = String.format("role=%s&username=%s",
            SocketRoles.ROLE_USER,
            username
        );

        socketWrapper.create();
        socketWrapper.connect();
        this.registerSocketEvents();
    }

    private void registerSocketEvents() {
        Socket s = socketWrapper.getSocket();

        s.on(SocketEvents.CATCHER_CONNECTION_SUCCESS,
        objects -> javafx.application.Platform.runLater(() -> {
            hideErrorMessage();
            ControllerManager.getInstance().changeController(ChatController.ID);
            user.setUsername(username.getText());
        }))

        .on(SocketEvents.CATCHER_CONNECTION_FAILED,
        objects -> javafx.application.Platform.runLater(() ->
            showErrorMessage(JSONObjectUtil.get("message", objects[0]))
        ))

        .on(SocketEvents.CATCHER_PROXY_LIST,
        objects -> javafx.application.Platform.runLater(() -> {
            JSONArray list = (JSONArray) objects[0];
            for (int i = 0, n = list.length(); i < n; i++) {
                try {
                    proxyManager.addProxyAddress(
                        (String) ((JSONObject) list.get(i)).get("host"),
                        (Integer) ((JSONObject) list.get(i)).get("port")
                    );
                } catch (JSONException ignored) {}
            }
        }));
    }

    private void showErrorMessage(String message) {
        error.setText(message);

        if (!error.isVisible()) {
            error.setVisible(true);
        }
    }

    private void hideErrorMessage() {
        error.setVisible(false);
    }

    @Override
    public void configure(Controller.Configurator configurator) {
        configurator
            .setId(ID)
            .setView(VIEW)
            .setMain(true)
        ;
    }
}
