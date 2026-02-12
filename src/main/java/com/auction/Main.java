package com.auction;

import com.auction.model.AuctionItem;
import javafx.animation.ScaleTransition;
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
import javafx.util.Duration;

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

        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;");

        //Scale transition for price label
        ScaleTransition scalePrice = new ScaleTransition(Duration.millis(150), priceLabel);
        scalePrice.setByX(0.1);
        scalePrice.setByY(0.1);
        scalePrice.setCycleCount(2); //Goes up and then back down
        scalePrice.setAutoReverse(true);

        //IT listens to priceProperty to trigger itself
        item.currentPriceProperty().addListener((observable, oldValue, newValue) -> {
            //First stop any transioins currently running
            scalePrice.stop();

            //Prevent stacking of transitions
            priceLabel.setScaleX(1);
            priceLabel.setScaleY(1);

            scalePrice.playFromStart();
        });

        HBox timerBox = new HBox(5);
        timerBox.setAlignment(Pos.CENTER_LEFT);

        Label timerIcon = new Label("⏱");
        timerIcon.setStyle("-fx-font-size: 20px;");

        Label timeValueLabel = new Label();
        timeValueLabel.textProperty().bind(item.timeLeftProperty().asString("%ds"));

        timerBox.getChildren().addAll(timerIcon, timeValueLabel);

        item.timeLeftProperty().addListener((obs, oldVal, newVal) -> {
            int secondsLeft = newVal.intValue();

            if (secondsLeft <= 10 && secondsLeft > 0) {
                // Turn the timer red and bold when under 10 seconds
                timerIcon.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16px;");
                timeValueLabel.setStyle("-fx-text-fill: red;");
            } else if (secondsLeft <= 0) {
                timeValueLabel.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
            }
        });

        //BID INPUT AREA
        HBox bidBox = new HBox(5);

        TextField bidField = new TextField();
        bidField.setPromptText("Bid..");
        bidField.setPrefWidth(100);

        Button bidButton = new Button("Place Bid");
        bidButton.setStyle("-fx-background-color: #1976d2; -fx-text-fill: white; -fx-font-weight: bold;");


        bidButton.setOnAction(e -> handleBid(item, bidField));

        bidBox.getChildren().addAll(bidField, bidButton);
        bidBox.setAlignment(Pos.CENTER_LEFT);

        //QUICK BID Buttons
        Button plus20 = new Button("+ $20");
        Button plus50 = new Button("+ $50");
        Button plus100 = new Button("+ $100");

        String quickButtonStyle = "-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;";
        plus20.setStyle(quickButtonStyle);
        plus50.setStyle(quickButtonStyle);
        plus100.setStyle(quickButtonStyle);

        plus20.setOnAction(e -> handleQuickBid(item, 20));
        plus50.setOnAction(e -> handleQuickBid(item, 50));
        plus100.setOnAction(e -> handleQuickBid(item, 100));

        HBox quickBidBox = new HBox(10);
        quickBidBox.setAlignment(Pos.CENTER);

        quickBidBox.getChildren().addAll(plus20, plus50, plus100);

        //Disable button when the timer dies
        item.timeLeftProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.intValue() <= 0) {
                bidButton.setDisable(true);
                bidField.setDisable(true);
                quickBidBox.setDisable(true);
                card.setStyle(card.getStyle() + "-fx-opacity: 0.6; -fx-background-color: #eeeeee;");

                //Update the scale of the final price to somewhat larger
                priceLabel.setScaleX(1.1);
                priceLabel.setScaleY(1.1);

            }
        });

        card.getChildren().addAll(nameLabel, new Separator(), priceLabel, timerBox, quickBidBox, bidBox);

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

    private void handleQuickBid(AuctionItem item, double increment) {
        double newBid = item.currentPriceProperty().doubleValue() + increment;
        item.placeBid(newBid);
    }

    static void main(String[] args) {
        launch(args);
    }
}
