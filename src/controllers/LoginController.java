package controllers;

import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import kraken.extension.scene.controller.Controller;
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

        SocketWrapper wrapper = (SocketWrapper) this.get("sensors.socket_wrapper");
        ObjectNode connectionParameters = (ObjectNode) this.get("parameters.connection");

        connectionParameters.put("username", username.getText());
        connectionParameters.put("password", password.getText());
        connectionParameters.put("role", SocketRoles.ROLE_USER);

        wrapper.connect(connectionParameters);
        this.registerEvents();
    }

    private void registerEvents() {
        SocketWrapper wrapper = (SocketWrapper) this.get("sensors.socket_wrapper");
        LoginController that = this;

        wrapper.getSocket().on("connection.success", objects -> javafx.application.Platform.runLater(() -> {
            that.hideErrorMessage();
            that.switchController((ChatController) this.get("controllers.chat_controller"));
        }));

        wrapper.getSocket().on("connection.failed", objects -> javafx.application.Platform.runLater(() -> {
            try {
                that.showErrorMessage(
                    ((JSONObject) objects[0]).get("message").toString()
                );
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
