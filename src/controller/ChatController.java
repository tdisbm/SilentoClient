package controller;

import com.fasterxml.jackson.databind.node.TextNode;
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
import javafx.scene.paint.Paint;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import kraken.extension.fx.controller.Controller;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.SocketWrapper;
import util.GridPaneUtil;
import util.JSONArrayUtil;

import java.io.File;
import java.util.Objects;

public class ChatController extends Controller {
    public static final String KEY_EVENT = "event";
    public static final String KEY_DESTINATION = "destination";

    public GridPane userList;
//    public GridPane roomList;
    public TextArea messageField;
    public Button sendButton;
    public TabPane activeBox;
    public WebView welcomeBox;

    private Socket socket;
    private User user;

    @Override
    public void init() {
        this.socket = ((SocketWrapper) this.get("services.socket_wrapper")).getSocket();
        this.user = (User) this.get("services.user_entity");

        this.registerSocketEvents();
        this.onMessageBoxInput();
        this.initWelcomeBox();
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

    private void initWelcomeBox() {
        String url = ((TextNode) this.get("parameters.silento_news_url")).asText();
        WebEngine engine = welcomeBox.getEngine();
        engine.load(url);
    }

    private void initialEmit() {
        this.socket.emit(SocketEvents.EMITTER_USER_NAME_LIST);
    }

    private void registerSocketEvents() {
        this.socket.on(SocketEvents.CATCHER_USER_NAME_LIST,
        objects -> javafx.application.Platform.runLater(() -> {
            JSONArray users = (JSONArray) objects[0];
            updateUserList(users);
            updateActiveBox(users);
        }));

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
            } catch (JSONException ignore) {}
        }));
    }

    private void addTabToActiveBox(String username, String event, boolean select) {
        Tab tab = new Tab();
        tab.setClosable(true);

        for (Tab t : activeBox.getTabs()) {
            if (Objects.equals(t.getText(), username)) {
                if (select) {
                    activeBox.getSelectionModel().select(t);
                }
                return;
            }
        }

        try {
            JSONObject userData = new JSONObject();
            userData.put(KEY_EVENT, event);
            userData.put(KEY_DESTINATION, username);

            ScrollPane sp = new ScrollPane();
            GridPane gp = new GridPane();
            sp.vvalueProperty().bind(gp.heightProperty());
            sp.setFitToWidth(true);
            sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            sp.setContent(gp);
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

    private void onMessageBoxInput() {
        messageField.setOnKeyPressed(keyEvent -> {
            KeyCode code = keyEvent.getCode();

            if (code == KeyCode.ENTER && keyEvent.isShiftDown()){
                String text = messageField.getText() + System.getProperty("line.separator");
                messageField.setText(text);
                messageField.positionCaret(text.length());
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

                image = new Image(new File("resources/views/img/send.png").toURI().toString());
                imageView.setImage(image);

                insets = new Insets(0,0,0,6);
                GridPane.setMargin(imageView, insets);

                userLabel = new Label();
                userLabel.setText(userName);
                userLabel.setCursor(javafx.scene.Cursor.OPEN_HAND);
                userLabel.setTextFill(Paint.valueOf("#08A5C2"));

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

    // TODO: Add source to check if is online to io.server
    private void updateActiveBox(JSONArray users) {
        int i = 0;
        for (Tab t: activeBox.getTabs()) {
            if (JSONArrayUtil.indexOf(users, t.getText()) == -1 && t.isClosable()) {
                activeBox.getTabs().remove(i);
            }
            i++;
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
            int rowCount = GridPaneUtil.countRows(gp);

            if (rowCount > -1) {
//                System.out.println("before: " + rowCount);
//                System.out.println("before: " + sp.getHeight());
                gp.addRow(rowCount, new Label(from + ":   " + message));
//                System.out.println("after: " + GridPaneUtil.countRows(gp));
//                System.out.println("after: " + sp.getHvalue());
//                System.out.println();

            }
        }
    }
}
