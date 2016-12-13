package controllers;

import entity.User;
import io.socket.client.Socket;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import kraken.extension.scene.controller.Controller;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.SocketWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ChatController extends Controller {
    public GridPane userList;
    public GridPane roomList;
    public TextArea messageField;
    public Button sendButton;
    public TabPane activeBox;

    private String currentDestination;
    private String currentSendEvent;
    private Socket socket;

    private boolean initialized = false;

    @FXML
    public void initialize() {
        this.socket = ((SocketWrapper) this.get("sensors.socket_wrapper")).getSocket();
        this.registerSocketEvents();
        this.onActiveBoxChange();
        this.onMessageBoxInput();
        this.initialEmit();
    }

    public void sendMessage() {
        if (!initialized) {
            this.initialize();
            initialized = true;
        }

        try {
            String message = messageField.getText().trim();

            if (message.isEmpty()) {
                return;
            }

            String username = ((User) this.get("sensors.user_entity")).getUsername();

            JSONObject params = new JSONObject();
            params.put("to", currentDestination);
            params.put("from", username);
            params.put("message", message);
            this.socket.emit(currentSendEvent, params);

            messageField.setText("");
            appendText(username, message, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initialEmit() {
        try {
            User user = (User) this.get("sensors.user_entity");
            JSONObject params = new JSONObject();

            params.put("exclude", new JSONArray("[\"" + user.getUsername() + "\"]"));

            this.socket.emit(SocketEvents.EMITTER_USER_NAME_LIST, params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void registerSocketEvents() {
        this.socket.on(SocketEvents.CATCHER_USER_NAME_LIST,
        objects -> javafx.application.Platform.runLater(() -> {
            updateUserList((JSONArray) objects[0]);
        }));

        socket.on(SocketEvents.CATCHER_MESSAGE_TO_USER,
        objects -> javafx.application.Platform.runLater(() -> {
            JSONObject message = (JSONObject) objects[0];
            try {
                appendText(
                    (String) message.get("from"),
                    (String) message.get("message"),
                    (String) message.get("from")
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));
    }

    private void addTabToActiveBox(String title) {
        Tab tab = new Tab();
        tab.setClosable(true);

        for (Tab t : activeBox.getTabs()) {
            if (Objects.equals(t.getText(), title)) {
                activeBox.getSelectionModel().select(t);
                return;
            }
        }

        ScrollPane sp = new ScrollPane();
        sp.setFitToWidth(true);
        sp.setContent(new GridPane());
        tab.setContent(sp);
        tab.setText(title);
        activeBox.getTabs().add(tab);
        activeBox.getSelectionModel().select(tab);
    }

    private void onUserButtonClick(Button b) {
        b.setOnMouseClicked(event -> {
            addTabToActiveBox(b.getText());
            currentSendEvent = SocketEvents.EMITTER_MESSAGE_TO_USER;
        });
    }

    private void onRoomButtonClick(Button b) {
        b.setOnMouseClicked(event -> {
            addTabToActiveBox(b.getText());
            currentSendEvent = SocketEvents.EMITTER_MESSAGE_TO_ROOM;
        });
    }

    private void onActiveBoxChange() {
        activeBox.getSelectionModel().selectedItemProperty().addListener(
            (ov, t, t1) -> {
                currentDestination = activeBox.getSelectionModel().getSelectedItem().getText();
            }
        );
    }

    private void onMessageBoxInput() {
        messageField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)  {
                sendButton.fire();
            }
        });
    }

    private void updateUserList(JSONArray users) {
        String row;
        Button userButton;

        userList.getChildren().clear();

        try {
            for (int i = 0; i < users.length(); i++) {
                row = (String) users.get(i);
                userButton = new Button();
                userButton.setText(row);
                userButton.setPadding(new Insets(5, 5, 5, 5));
                userButton.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(userButton, Priority.ALWAYS);
                onUserButtonClick(userButton);
                userList.addRow(i, userButton);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void appendText(String from, String message, String tabName) {
        ScrollPane sp = null;

        if (tabName != null) {
            for (Tab t : activeBox.getTabs()) {
                if (Objects.equals(t.getText(), tabName)) {
                    sp = (ScrollPane) t.getContent();
                    break;
                }
            }
        } else {
            sp = (ScrollPane) activeBox.getSelectionModel().getSelectedItem().getContent();
        }

        GridPane gp = (GridPane) sp.getContent();
        if (gp != null) {
            int rowCount = countGridPaneRows(gp);

            if (rowCount > -1) {
                gp.addRow(rowCount, new Label(from + ":   " + message));
            }
        }
    }

    private int countGridPaneRows(GridPane pane) {
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
