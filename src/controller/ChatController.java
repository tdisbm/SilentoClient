package controller;

import com.fasterxml.jackson.databind.node.TextNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import entity.Message;
import entity.User;
import io.socket.client.Socket;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import kraken.Kraken;
import kraken.unit.Container;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import services.AvatarCollection;
import services.SocketWrapper;
import services.proxy.ProxyManager;
import services.proxy.ProxyServer;
import tasks.RSAKeyGenerator;
import util.*;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class ChatController extends Controller {
    public static final String ID = "chat_controller";
    public static final String VIEW = "resources/views/chat.fxml";

    private static final String KEY_EVENT = "event";
    private static final String KEY_DESTINATION = "destination";

    public GridPane userList;

    public TextArea messageField;
    public Button sendButton;
    public TabPane activeBox;
    public WebView welcomeBox;

    private Socket socket;
    private User user;
    private Message messagePacket;
    private ProxyManager proxyManager;
    private Gson gson;
    private AvatarCollection avatarCollection;
    private RSAKeyGenerator rsaKeyGenerator;
    private LinkedHashMap<String, PublicKey> tunnelingMap;
    private File messageSound;

    public ChatController() {
        Container container = Kraken.getInstance().getContainer();
        avatarCollection = container.get("services.avatar_collection");
        rsaKeyGenerator = container.get("tasks.rsa_key_generator");
        proxyManager = container.get("services.proxy_manager");
        messagePacket = new Message();
        messageSound = new File("resources/sound/sound.wav");
        tunnelingMap = new LinkedHashMap<>();
        socket = ((SocketWrapper) container.get("services.socket_wrapper")).getSocket();
        user = container.get("services.user_entity");
        gson = new GsonBuilder().create();
    }

    @Override
    public void onCreate() {
        this.registerSocketEvents();
        this.onMessageBoxInput();
        this.initProxyManager();
        this.initWelcomeBox();
        this.initialEmit();
    }

    public void sendMessage() {
        String message = messageField.getText().trim();
        JSONObject userData = (JSONObject) activeBox
            .getSelectionModel()
            .getSelectedItem()
            .getUserData()
        ;

        if (message.isEmpty() || userData == null) {
            return;
        }

        String to = JSONObjectUtil.get(KEY_DESTINATION, userData);
        String event = JSONObjectUtil.get(KEY_EVENT, userData);
        String from = this.user.getUsername();

        PublicKey publicKey = tunnelingMap.get(to);

        appendText(from, message, null);
        if (publicKey != null) {
            message = RSAEncryptionUtil.encrypt(message, publicKey);
        }

        messagePacket
            .setTo(to)
            .setEvent(event)
            .setFrom(from)
            .setMessage(message)
        ;

        proxyManager.proxify(gson.toJson(messagePacket));
        messageField.clear();
    }

    private void initProxyManager() {
        this.proxyManager.setSecureKey("not_so_secure");
        this.proxyManager

        .on(ProxyServer.EVENT_PROXY_TERMINATE, result ->
        javafx.application.Platform.runLater(() -> {
            Message message = gson.fromJson((String) result[0], Message.class);
            String event = message.getEvent();
            message.setEvent(null);
            this.socket.emit(event, message.toJsonObject());
        }))

        .on(ProxyServer.EVENT_PROXY_SERVER_IS_UP, objects ->
        javafx.application.Platform.runLater(() -> {
            String host = (String) objects[0];
            int port = (Integer) objects[1];

            try {
                JSONObject json = new JSONObject();
                json.put("host", host);
                json.put("port", port);
                socket.emit(SocketEvents.EMITTER_PROXY_LIST_UPDATE, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));
    }

    private void initWelcomeBox() {
        TextNode url = Kraken.getInstance().getContainer().get("parameters.silento_news_url");
        WebEngine engine = welcomeBox.getEngine();
        engine.load(url.asText());
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
            String from = JSONObjectUtil.get("from", objects[0]);
            String message = JSONObjectUtil.get("message", objects[0]);

            if (message != null) {
                SoundPlayer.playSound(messageSound);
                message = RSAEncryptionUtil.decrypt(message, rsaKeyGenerator.getPrivateKey());
                addTabToActiveBox(from, SocketEvents.EMITTER_MESSAGE_TO_USER, false);
                appendText(from, message, from);
            }
        }));

        socket.on(SocketEvents.CATCHER_TUNNELING_REQUEST,
        objects -> javafx.application.Platform.runLater(() -> {
            String from = JSONObjectUtil.get("from", objects[0]);
            String publicKey = JSONObjectUtil.get("public_key", objects[0]);

            try {
                tunnelingMap.put(from, RSAEncryptionUtil.loadPublicKey(publicKey));
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }

            LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
            parameters.put("from", user.getUsername());
            parameters.put("to", from);
            parameters.put("public_key", rsaKeyGenerator.getSerializedPublicKey());
            socket.emit(SocketEvents.EMITTER_TUNNELING_CONFIRM, parameters);
        }));

        socket.on(SocketEvents.CATCHER_TUNNELING_CONFIRM,
        objects -> javafx.application.Platform.runLater(() -> {
            String from = JSONObjectUtil.get("from", objects[0]);
            String publicKey = JSONObjectUtil.get("public_key", objects[0]);

            try {
                tunnelingMap.put(from, RSAEncryptionUtil.loadPublicKey(publicKey));
            } catch (GeneralSecurityException | IOException e) {
                e.printStackTrace();
            }
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
        l.setOnMouseClicked(event -> {
            LinkedHashMap<String, String> parameters = new LinkedHashMap<>();
            parameters.put("from", user.getUsername());
            parameters.put("to", l.getText());
            parameters.put("public_key", rsaKeyGenerator.getSerializedPublicKey());

            socket.emit(SocketEvents.EMITTER_TUNNELING_REQUEST, parameters);
            addTabToActiveBox(l.getText(), SocketEvents.EMITTER_MESSAGE_TO_USER, true);
        });
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

                image = new Image(new File(avatarCollection.random()).toURI().toString());
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
        ObservableList<Tab> toRemove = FXCollections.observableArrayList();
        toRemove.addAll(activeBox.getTabs()
            .stream()
            .filter(t -> JSONArrayUtil.indexOf(users, t.getText()) == -1 && t.isClosable())
            .collect(Collectors.toList())
        );

        for (Tab t : toRemove) {
            activeBox.getTabs().remove(t);
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
                gp.addRow(rowCount, new Label(from + ":   " + message));
            }
        }
    }

    @Override
    public void configure(Controller.Configurator configurator) {
        configurator
            .setId(ID)
            .setView(VIEW)
        ;
    }
}
