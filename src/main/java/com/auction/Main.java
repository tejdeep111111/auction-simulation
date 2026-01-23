package com.auction;

import com.auction.model.AuctionItem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;


public class Main extends Application {
    @Override
    public void start(Stage stage) {
        AuctionItem item1 = new AuctionItem("Guitar", 500.0, 60);

        Label nameLabel = new Label("Item: " + item1.getName());
        Label priceLabel = new Label();
        Label durationLabel = new Label();

        priceLabel.textProperty().bind(item1.currentPriceProperty().asString("Current Bid %.2f"));
        durationLabel.textProperty().bind(item1.timeLeftProperty().asString("Time Left: %d"));

        TextField bidInput = new TextField();
        bidInput.setPromptText("Enter bid amount");
        Button bidButton = new Button("Place Bid");
        bidButton.setOnAction( e -> {
            try{
                double amount = Double.parseDouble(bidInput.getText());
                if(!item1.placeBid(amount)) {
                    showError("Bid too low!");
                }
                bidInput.clear();
            } catch (NumberFormatException ex) {
                showError("Invalid amount!");
            }
        });

        startAuctionTimer(item1);

        VBox root = new VBox(15, nameLabel, priceLabel, durationLabel, bidInput, bidButton);
        root.setPadding(new Insets(20));
        stage.setScene(new Scene(root, 300, 250));
        stage.setTitle("Auction Simulation");
        stage.show();
    }

    private void startAuctionTimer(AuctionItem item) {
        Thread timerThread = new Thread(() -> {
            while(item.getTimeLeft() > 0) {
                try {
                    Thread.sleep(1000); //1second
                    Platform.runLater(() -> item.setTimeLeft(item.getTimeLeft() - 1));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            Platform.runLater(() -> showError("Auction Ended!"));
            Platform.exit();
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message);
        alert.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}
