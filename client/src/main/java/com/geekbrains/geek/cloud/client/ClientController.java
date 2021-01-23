package com.geekbrains.geek.cloud.client;

import com.geekbrains.geek.cloud.common.ProtoFileSender;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientController implements Initializable {
    private ClientNetty client;
    private boolean isAuthorized;

    @FXML
    ListView<String> clientfilesList;

    @FXML
    ListView<String> serverfilelist;

    @FXML
    StackPane upperPanel;

    @FXML
    VBox bottomPanel;

    @FXML
    TextField loginField;

    @FXML
    PasswordField passField;

    @FXML
    TextField nickField;

    @FXML
    TextArea messageErr;

    @FXML
    ProgressBar loading;

    private DoubleProperty progress;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        client = new ClientNetty(this);
        client.run();
        System.out.println("Подключение к серверу прошло успешно");
        progress = new SimpleDoubleProperty(0);
        refreshLocalFilesList();
        progress = new SimpleDoubleProperty(0);
        loading.progressProperty().bind(progress);
    }

    public void pressOnUploadBtn(ActionEvent actionEvent) {
        if (clientfilesList!=null && clientfilesList.getSelectionModel().getSelectedItem()!=null) {
            try {
                ProtoFileSender.sendFile(Paths.get("client_repository/" + clientfilesList.getSelectionModel().getSelectedItem()), client.getClientChannel(), future -> {
                    if (!future.isSuccess()) {
                        future.cause().printStackTrace();
                    }
                    if (future.isSuccess()) {
                        System.out.println("Файл "+clientfilesList.getSelectionModel().getSelectedItem()+" успешно передан");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void pressOnDownloadBtn(ActionEvent actionEvent) {
        if (serverfilelist!=null && serverfilelist.getSelectionModel().getSelectedItem()!=null) {
            ProtoFileSender.sendCommand("/download "+serverfilelist.getSelectionModel().getSelectedItem(),client.getClientChannel(), null);
        }
    }

    public void pressOnDeleteBtn(ActionEvent actionEvent) throws IOException {
        if (clientfilesList!=null && clientfilesList.getSelectionModel().getSelectedItem()!=null && clientfilesList.isFocused()) {
            Files.delete(Paths.get("client_repository/" + clientfilesList.getSelectionModel().getSelectedItem()));
            clientfilesList.getSelectionModel().clearSelection();
            refreshLocalFilesList();
        }
        if (serverfilelist!=null && serverfilelist.getSelectionModel().getSelectedItem()!=null && serverfilelist.isFocused()) {
            ProtoFileSender.sendCommand("/delete "+serverfilelist.getSelectionModel().getSelectedItem(),client.getClientChannel(), null);
            serverfilelist.getSelectionModel().clearSelection();
        }
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
        if (!isAuthorized) {
            upperPanel.setVisible(true);
            upperPanel.setManaged(true);
            bottomPanel.setVisible(false);
            bottomPanel.setManaged(false);
        } else {
            upperPanel.setVisible(false);
            upperPanel.setManaged(false);
            bottomPanel.setVisible(true);
            bottomPanel.setManaged(true);
        }
    }

    public void tryToAuth(ActionEvent actionEvent) {
        if (loginField.getText().equals("") || passField.getText().equals("")) {
            messageErr.setText("Ошибка ввода данных.\nПоля ввода данных не должны быть пустыми, кроме никнейма");
        } else {
            ProtoFileSender.sendCommand("/auth " + loginField.getText() + " " + passField.getText(), client.getClientChannel(), null);
            loginField.clear();
            passField.clear();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        Pattern p = Pattern.compile("^\\S+$");
        Matcher m_login = p.matcher(loginField.getText());
        Matcher m_pass = p.matcher(passField.getText());
        Matcher m_nick = p.matcher(nickField.getText());
        if (m_login.matches() && m_pass.matches() && m_nick.matches()) {
            ProtoFileSender.sendCommand("/regin " + loginField.getText() + " " + passField.getText() + " " + nickField.getText(), client.getClientChannel(),null);
            loginField.clear();
            passField.clear();
            nickField.clear();
        } else if (loginField.getText().equals("") || passField.getText().equals("") || nickField.getText().equals("")) {
            messageErr.setText("Ошибка ввода данных.\nПоля ввода данных не должны быть пустыми");
        } else {
            messageErr.setText("Ошибка ввода данных.\nВсе данные должны быть без пробелов");
            loginField.clear();
            passField.clear();
            nickField.clear();
        }
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                clientfilesList.getItems().clear();
                Files.list(Paths.get("client_repository")).map(p -> p.getFileName().toString()).forEach(o -> clientfilesList.getItems().add(o));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void refreshServerFileList(String[] files) {
        updateUI(() -> {
            serverfilelist.getItems().clear();
            if (files[0].length()>0)
                Arrays.stream(files).forEach(o -> serverfilelist.getItems().add(o));
        });
    }

    public static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    public void updateErrorMsg(String msg) {
        messageErr.setText(msg);
    }

    public void resetProgress() {
        progress.setValue(0);
    }

    public DoubleProperty getProgress() {
        return progress;
    }

    public void addProgress(double addValue, long max) {
        progress.setValue(addValue/max);
    }
}
