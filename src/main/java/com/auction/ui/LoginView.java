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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/login-view-logo.png")));
        logo.setFitHeight(100);
        logo.setPreserveRatio(true);

        Label title = new Label("Auction Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField userField = new TextField();
        //Common width for all
        double commonWidth = 250;
        userField.setMaxWidth(commonWidth);
        userField.setPromptText("Username");
        String textFieldStyle = "-fx-background-radius: 15; " + "-fx-border-radius: 15; " + "-fx-background-color: white; " +
                "-fx-border-color: #848484; " + "-fx-focus-color: transparent; " + "-fx-faint-focus-color: transparent;";
        userField.setStyle(textFieldStyle);

        Image passSeeIcon = new Image(getClass().getResourceAsStream("/password-see.jpg"));
        Image passUnseeIcon = new Image(getClass().getResourceAsStream("/password-unsee.jpg"));
        //By default unsee
        ImageView eyeView = new ImageView(passSeeIcon);
        eyeView.setFitWidth(20);
        eyeView.setPreserveRatio(true);

        //2 different fields for each see and unsee
        PasswordField hiddenField = new PasswordField();
        hiddenField.setPromptText("Password");

        TextField shownField = new TextField();
        shownField.setPromptText("Password");
        shownField.setVisible(false);
        shownField.setManaged(false);

        //Synchronze the text between the two fields so that user can switch in between
        shownField.textProperty().bindBidirectional(hiddenField.textProperty());

        //Toggle button with eye logo
        Button toggleButton = new Button();
        toggleButton.setGraphic(eyeView);
        toggleButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

        toggleButton.setOnAction(e -> {
            if(hiddenField.isVisible()) {
                //Show password
                eyeView.setImage(passUnseeIcon);
                hiddenField.setVisible(false);
                hiddenField.setManaged(false);
                shownField.setVisible(true);
                shownField.setManaged(true);
            } else {
                //Hide password
                eyeView.setImage(passSeeIcon);
                shownField.setVisible(false);
                shownField.setManaged(false);
                hiddenField.setVisible(true);
                hiddenField.setManaged(true);
            }
        });

        hiddenField.setStyle(textFieldStyle);
        shownField.setStyle(textFieldStyle);

        //Container for the password fields
        StackPane fieldStack = new StackPane(hiddenField, shownField);

        //Container for the fields + eye button
        StackPane passwordContainer = new StackPane(fieldStack, toggleButton);
        passwordContainer.setMaxWidth(commonWidth);
        StackPane.setAlignment(toggleButton, Pos.CENTER_RIGHT);

        Button loginButton = new Button("Login");
        //ENTER triggers loginButton
        loginButton.setDefaultButton(true);
        loginButton.setMinWidth(commonWidth);
        loginButton.setPrefHeight(37);
        loginButton.setStyle("-fx-background-color: #1976d2; -fx-border-radius: 37px; -fx-background-radius: 37px; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white; -fx-cursor: hand;");

        loginButton.setOnAction(e -> {
            User user = UserDAO.authenticate(userField.getText(), hiddenField.getText());
            if(user!=null) {
                SessionManager.setCurrentUser(user);
                //Making that set of code run here
                onSuccess.run();
            } else {
                Main.showToast("Invalid Credentials");
            }
        });

        root.getChildren().addAll(logo, title, userField, passwordContainer, loginButton);
        stage.setScene(new Scene(root, 400, 450));
        stage.setMinHeight(400);
        stage.setMinWidth(300);
        stage.setTitle("Login - Auction Simulator");
    }
}
