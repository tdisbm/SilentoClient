package controller;

import com.fasterxml.jackson.databind.node.TextNode;
import entity.User;
import io.socket.client.IO;
import io.socket.client.Socket;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import kraken.extension.fx.controller.Controller;
import services.SocketRoles;
import services.SocketWrapper;
import util.Constraints;
import util.JSONObjectUtil;
import util.SocketEvents;

public class LoginController extends Controller {
    public final static String MESSAGE_MANDATORY_USERNAME = "username is mandatory";

    public TextField username;
    public Text error;

    private SocketWrapper socketWrapper;
    private User user;

    @Override
    public void init() {
        this.getStage().setResizable(false);
        this.socketWrapper = this.get("services.socket_wrapper");
        this.user = this.get("services.user_entity");

        TextNode url = this.get("parameters.silento_server_url");
        socketWrapper.initialize(url.asText(), new IO.Options());

        this.registerUsernameEvents();
    }

    public void registerUsernameEvents() {
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
            switchController(this.get("controllers.chat_controller"));
            user.setUsername(username.getText());
        }));

        s.on(SocketEvents.CATCHER_CONNECTION_FAILED,
        objects -> javafx.application.Platform.runLater(() ->
            showErrorMessage(JSONObjectUtil.get("message", objects[0]))
        ));
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
}
