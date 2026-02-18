package com.auction;


import com.auction.model.AuctionItem;
import com.auction.ui.LoginView;
import com.auction.ui.MainDashboard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static com.auction.database.ItemDAO.updatePriceInDB;
import static javafx.application.Application.launch;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        try {
            // Option A: If logo is in your resources folder
            Image icon = new Image(getClass().getResourceAsStream("/app-logo.png"));
            stage.getIcons().add(icon);
        } catch (Exception e) {
            System.out.println("App icon could not be loaded: " + e.getMessage());
        }
        LoginView loginView = new LoginView();

        //onSuccess runnable task
        loginView.show(stage, () -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.show(stage);
        });

        stage.setTitle("Auction PRO");
        stage.show();
    }

    public static void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }


    static void main(String[] args) {
        launch(args);
    }
}
