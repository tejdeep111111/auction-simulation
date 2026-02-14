package com.auction;


import com.auction.model.AuctionItem;
import com.auction.ui.LoginView;
import com.auction.ui.MainDashboard;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import static com.auction.database.ItemDAO.updatePriceInDB;
import static javafx.application.Application.launch;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        LoginView loginView = new LoginView();

        //onSuccess runnable task
        loginView.show(stage, () -> {
            MainDashboard dashboard = new MainDashboard();
            dashboard.show(stage);
        });
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
