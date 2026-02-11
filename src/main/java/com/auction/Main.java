package com.auction;

import com.auction.model.AuctionItem;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.control.*;

import java.util.Arrays;
import java.util.List;


public class Main extends Application {
    @Override
    public void start(Stage stage) {
        List<AuctionItem> items = Arrays.asList(
                new AuctionItem("Electric Guitar", 500.0, 120),
                new AuctionItem("Rare Coin", 1500.0, 45),
                new AuctionItem("Modern Art", 3000.0, 300),
                new AuctionItem("Vintage Camera", 250.0, 60),
                new AuctionItem("Gaming Console", 400.0, 90),
                new AuctionItem("Designer Watch", 2200.0, 15)
        );

        //Using the layout FLOWPANE

        FlowPane flowPane = new FlowPane();
        flowPane.setPadding(new Insets(20));
        flowPane.setHgap(20);
        flowPane.setVgap(20);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setStyle("-fx-background-color: #f4f4f4;");

        for(AuctionItem item : items) {
            flowPane.getChildren().add(createItemCard(item));
            startAuctionTimer(item);
        }

        ScrollPane root = new ScrollPane(flowPane);
        root.setFitToWidth(true);

        Scene scene = new Scene(root, 850, 600);
        stage.setScene(scene);
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
        });
        timerThread.setDaemon(true);
        timerThread.start();
    }

    private VBox createItemCard(AuctionItem item) {
        VBox card = new VBox(10);
        card.setPrefWidth(250);
        card.setPadding(new Insets(15));

        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Label priceLabel = new Label();
        priceLabel.textProperty().bind(item.currentPriceProperty().asString("$%.2f"));

        Label timeLabel = new Label();
        timeLabel.textProperty().bind(item.timeLeftProperty().asString("Ends in: %ds"));

        //BID INPUT AREA
        HBox bidBox = new HBox(5);

        TextField bidField = new TextField();
        bidField.setPromptText("Bid..");
        bidField.setPrefWidth(100);

        Button bidButton = new Button("Place Bid");
        bidButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");

        //Disable button when the timer dies
        item.timeLeftProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.intValue() <= 0) {
                bidButton.setDisable(true);
                bidField.setDisable(true);
                card.setStyle(card.getStyle() + "-fx-opacity: 0.6; -fx-background-color: #eeeeee;");
            }
        });

        bidButton.setOnAction(e -> handleBid(item, bidField));

        bidBox.getChildren().addAll(bidField, bidButton);
        bidBox.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(nameLabel, new Separator(), priceLabel, timeLabel, bidBox);

        return card;
    }

    private void handleBid(AuctionItem item, TextField field) {
        try {
            double amount = Double.parseDouble(field.getText());
            if(!item.placeBid(amount)) {
                showToast("Bid too low!!");
            }
            field.clear();
        } catch (NumberFormatException e) {
            showToast("Numbers only!!");
        }
    }

    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    static void main(String[] args) {
        launch(args);
    }
}
