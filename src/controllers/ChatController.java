package controllers;

import entity.User;
import io.socket.client.Socket;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;
import kraken.extension.fx.controller.Controller;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.SocketWrapper;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public class ChatController extends Controller {
    public static final String KEY_EVENT = "event";
    public static final String KEY_DESTINATION = "destination";

    public GridPane userList;
//    public GridPane roomList;
    public TextArea messageField;
    public Button sendButton;
    public TabPane activeBox;

    private String currentSendEvent;
    private Socket socket;
    private User user;

    @Override
    public void init() {
        this.socket = ((SocketWrapper) this.get("services.socket_wrapper")).getSocket();
        this.user = (User) this.get("services.user_entity");
        this.registerSocketEvents();
        this.onActiveBoxChange();
        this.onMessageBoxInput();
        this.initialEmit();
    }

    public void sendMessage() {
        try {
            String message = messageField.getText().trim();
            JSONObject userData = (JSONObject) activeBox
                .getSelectionModel()
                .getSelectedItem()
                .getUserData()
            ;

            if (userData == null) {
                return;
            }

            String destination = (String) userData.get(KEY_DESTINATION);
            String event = (String) userData.get(KEY_EVENT);

            if (message.isEmpty() || destination.isEmpty() || event.isEmpty()) {
                return;
            }

            String username = this.user.getUsername();

            JSONObject params = new JSONObject();
            params.put("to", destination);
            params.put("from", username);
            params.put("message", message);
            this.socket.emit(event, params);

            messageField.clear();
            appendText(username, message, null);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initialEmit() {
        this.socket.emit(SocketEvents.EMITTER_USER_NAME_LIST);
    }

    private void registerSocketEvents() {
        this.socket.on(SocketEvents.CATCHER_USER_NAME_LIST,
        objects -> javafx.application.Platform.runLater(() ->
            updateUserList((JSONArray) objects[0]))
        );

        socket.on(SocketEvents.CATCHER_MESSAGE_TO_USER,
        objects -> javafx.application.Platform.runLater(() -> {
            JSONObject message = (JSONObject) objects[0];
            try {
                addTabToActiveBox(
                    (String) message.get("from"),
                    SocketEvents.EMITTER_MESSAGE_TO_USER,
                    false
                );
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

    private void addTabToActiveBox(String username, String event, boolean select) {
        Tab tab = new Tab();
        tab.setClosable(true);

        for (Tab t : activeBox.getTabs()) {
            if (Objects.equals(t.getText(), username)) {
                activeBox.getSelectionModel().select(t);
                return;
            }
        }

        try {
            JSONObject userData = new JSONObject();
            userData.put(KEY_EVENT, event);
            userData.put(KEY_DESTINATION, username);

            ScrollPane sp = new ScrollPane();
            sp.setFitToWidth(true);
            sp.setContent(new GridPane());
            tab.setContent(sp);
            tab.setText(username);
            tab.setUserData(userData);
            activeBox.getTabs().add(tab);
            if (select) {
                activeBox.getSelectionModel().select(tab);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onUserLabelClick(Label l) {
        l.setOnMouseClicked(event ->
            addTabToActiveBox(l.getText(), SocketEvents.EMITTER_MESSAGE_TO_USER, true)
        );
    }

//    private void onRoomButtonClick(Button b) {
//        b.setOnMouseClicked(event -> {
//            addTabToActiveBox(b.getText(), true);
//            currentSendEvent = SocketEvents.EMITTER_MESSAGE_TO_ROOM;
//        });
//    }

    private void onActiveBoxChange() {
//        activeBox.getSelectionModel().selectedItemProperty().addListener(
//            (ov, t, t1) -> {
//                currentDestination = activeBox.getSelectionModel().getSelectedItem().getText();
//                currentSendEvent = SocketEvents.EMITTER_MESSAGE_TO_USER;
//            }
//        );
    }

    private void onMessageBoxInput() {
        messageField.setOnKeyPressed(keyEvent -> {
            KeyCode code = keyEvent.getCode();

            if (code == KeyCode.SHIFT){
                return;
            }

            if (code == KeyCode.ENTER)  {
                sendButton.fire();
            }
        });
    }

    private void updateUserList(JSONArray users) {
        String userName;
        Label userLabel;
        ImageView imageView;
        Insets insets;
        Image image;

        String currentUserName = this.user.getUsername();

        userList.getChildren().clear();

        try {
            for (int i = 0, row = 0; i < users.length(); i++) {
                userName = (String) users.get(i);

                if (Objects.equals(currentUserName, userName)) {
                    if (row > 0) {
                        row--;
                    }
                    continue;
                }

                imageView = new ImageView();
                imageView.setFitHeight(55.0);
                imageView.setFitWidth(55.0);
                imageView.setPickOnBounds(true);
                imageView.setPreserveRatio(true);

                GridPane.setHalignment(imageView, HPos.LEFT);
                GridPane.setValignment(imageView, VPos.CENTER);

                image = new Image(new File("../../resources/views/img/send.png").toURI().toString());
                imageView.setImage(image);

                insets = new Insets(0,0,0,6);
                GridPane.setMargin(imageView, insets);

                userLabel = new Label();
                userLabel.setText(userName);

                userLabel.setTextAlignment(TextAlignment.CENTER);
                GridPane.setColumnIndex(userLabel, i);
                GridPane.setHalignment(userLabel, HPos.CENTER);
                GridPane.setValignment(userLabel, VPos.CENTER);
                onUserLabelClick(userLabel);
                userList.addRow(row, imageView, userLabel);
                row++;
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

        assert sp != null;
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
