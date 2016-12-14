package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import entity.User;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import kraken.extension.fx.controller.Controller;
import org.json.JSONException;
import org.json.JSONObject;
import services.SocketRoles;
import services.SocketWrapper;

public class LoginController extends Controller {
    public final static String MESSAGE_INVALID_CREDENTIALS = "incorrect password or username!";

    public TextField username;
    public PasswordField password;
    public Label warning;

    public void loginAction() {
        if (!this.validateInput()) {
            this.showErrorMessage(MESSAGE_INVALID_CREDENTIALS);
            return;
        }

        SocketWrapper wrapper = (SocketWrapper) this.get("services.socket_wrapper");
        ObjectNode connectionParameters = (ObjectNode) this.get("parameters.connection");

        connectionParameters.put("username", username.getText());
        connectionParameters.put("password", password.getText());
        connectionParameters.put("role", SocketRoles.ROLE_USER);

        wrapper.connect(connectionParameters);
        this.registerEvents();
    }

    private void registerEvents() {
        SocketWrapper wrapper = (SocketWrapper) this.get("services.socket_wrapper");
        LoginController that = this;
        ChatController chatController = (ChatController) this.get("controllers.chat_controller");
        User user = (User) that.get("services.user_entity");

        wrapper.getSocket().on(SocketEvents.CATCHER_CONNECTION_SUCCESS, objects -> javafx.application.Platform.runLater(() -> {
            that.hideErrorMessage();
            that.switchController(chatController);
            user.setUsername(username.getText());
        }));

        wrapper.getSocket().on(SocketEvents.CATCHER_CONNECTION_FAILED, objects -> javafx.application.Platform.runLater(() -> {
            try {
                that.showErrorMessage(((JSONObject) objects[0]).get("message").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));
    }

    private boolean validateInput() {
        return !(
            username.getText().isEmpty() &&
            password.getText().isEmpty()
        );
    }

    private void showErrorMessage(String message) {
        warning.setText(message);

        if (!warning.isVisible()) {
            warning.setVisible(true);
        }
    }

    private void hideErrorMessage() {
        warning.setVisible(false);
    }
}
