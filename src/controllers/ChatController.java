package controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import kraken.extension.scene.controller.Controller;
import org.json.JSONArray;
import org.json.JSONException;
import services.SocketWrapper;

import java.util.Arrays;
import java.util.Objects;

public class ChatController extends Controller {
    public GridPane userList;
    public GridPane roomList;
    public TextField messageField;
    public Button sendButton;
    public TabPane activeBox;
    private String currentDestination;
    private String currentSendEvent;
    private boolean initialized = false;

    private void registerSocketEvents() {
        SocketWrapper wrapper = (SocketWrapper) this.get("sensors.socket_wrapper");

        wrapper.getSocket().on("user_name_list", objects -> javafx.application.Platform.runLater(() -> {
            JSONArray users = (JSONArray) objects[0];
            String row;
            Button userButton;

            userList.getChildren().removeAll();

            try {
                for (int i = 0; i < users.length(); i++) {
                    row = (String) users.get(i);
                    userButton = new Button();
                    userButton.setText(row);
                    userButton.setPadding(new Insets(5,5,5,5));
                    userButton.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(userButton, Priority.ALWAYS);
                    appendUserButtonEvent(userButton);
                    userList.addRow(i, userButton);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            System.out.println(Arrays.toString(objects));
        }));
        wrapper.getSocket().emit("user_name_list");
    }

    public void appendUserButtonEvent(Button b) {
        b.setOnMouseClicked(event -> {

        });
    }

    private void addTabToActiveBox(String title) {
        Tab tab = new Tab();
        tab.setClosable(true);

        for (Tab t : activeBox.getTabs()) {
            if (Objects.equals(t.getText(), title)) {
                activeBox.getSelectionModel().select(tab);
                return;
            }
        }

        tab.setText(title);
        activeBox.getTabs().add(tab);
        activeBox.getSelectionModel().select(tab);
    }
//
//    private void onActiveBoxChange() {
//        activeBox.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
//            if (newTab == tabPresentation) {
//                comboBoxPresYear.setVisible(true);
//                lblPresYear.setVisible(true);
//            }
//        });
//    }

    @FXML
    public void initialize() {
        this.registerSocketEvents();
//        this.onActiveBoxChange();
    }

    public void sendMessage() {
        if (!this.initialized) {
            this.initialize();
            this.initialized = true;
        }
    }
}
