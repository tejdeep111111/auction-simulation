package com.auction.ui;

import com.auction.Main;
import com.auction.database.UserDAO;
import com.auction.model.User;
import com.auction.util.SessionManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.Label;

public class LoginView {
    //Runnable is an Functional Interface, we use a lambda expression to set some lines of code to it so that this set of code will be executed without going elsewhere out of this fn
    public void show(Stage stage, Runnable onSuccess) {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: white");

        Label title = new Label("Auction Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        userField.setMaxWidth(250);
        userField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(250);
        passwordField.setPromptText("Password");

        Button loginButton = new Button("Login");
        loginButton.setMinWidth(250);
        loginButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white;");

        loginButton.setOnAction(e -> {
            User user = UserDAO.authenticate(userField.getText(), passwordField.getText());
            if(user!=null) {
                SessionManager.setCurrentUser(user);
                //Making that set of code run here
                onSuccess.run();
            } else {
                Main.showToast("Invalid Credentials");
            }
        });

        root.getChildren().addAll(title, userField, passwordField, loginButton);
        stage.setScene(new Scene(root, 400, 450));
        stage.setTitle("Login - Auction Simulator");
    }
}
