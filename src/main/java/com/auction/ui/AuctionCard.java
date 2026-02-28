package com.auction.ui;

import com.auction.Main;
import com.auction.database.ItemDAO;
import com.auction.model.AuctionItem;
import com.auction.util.SessionManager;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class AuctionCard extends VBox{
    public AuctionCard(AuctionItem item) {
        this.setSpacing(10);
        this.setPrefWidth(250);
        this.setPadding(new Insets(15));
        //
        this.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        setupFavoriteStar(item);

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


        bidButton.setOnAction(e -> {
            if(handleBid(item, bidField)) {
                bidField.clear();
            }
        });

        bidBox.getChildren().addAll(bidField, bidButton);
        bidBox.setAlignment(Pos.CENTER_LEFT);


        HBox quickBidBox = new HBox(10);
        quickBidBox.setAlignment(Pos.CENTER);

        quickBidBox.getChildren().addAll(createQuickButton(item, 20), createQuickButton(item, 50), createQuickButton(item, 100));

        //Disable button when the timer dies
        item.timeLeftProperty().addListener((obs, oldVal, newVal) -> {
            if(newVal.intValue() <= 0) {
                bidButton.setDisable(true);
                bidField.setDisable(true);
                quickBidBox.setDisable(true);
                this.setStyle(this.getStyle() + "-fx-opacity: 0.6; -fx-background-color: #eeeeee;");

                //Update the scale of the final price to somewhat larger
                priceLabel.setScaleX(1.1);
                priceLabel.setScaleY(1.1);

            }
        });

        this.getChildren().addAll(nameLabel, new Separator(), priceLabel, timerBox, quickBidBox, bidBox);
    }

    private Button createQuickButton(AuctionItem item, int amount) {
        Button button = new Button("+$" + amount);
        button.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #ccc; -fx-border-radius: 5; -fx-background-radius: 5;");

        button.setOnAction(e -> {
            double newPrice = item.currentPriceProperty().get() + amount;
            attemptBid(item, newPrice);
        });

        return button;
    }

    private boolean handleBid(AuctionItem item, TextField field) {
        try {
            double amount = Double.parseDouble(field.getText());

            if(attemptBid(item, amount)) {
                return true;
            } else {
                Main.showToast("Bid too low!!");
                return false;
            }
        } catch (NumberFormatException e) {
            Main.showToast("Numbers only!!");
            return false;
        }
    }

    private void setupFavoriteStar(AuctionItem item) {
        Label star = new Label();
        int userId = SessionManager.getCurrentUser().getUser_Id();
        int itemID = item.getId();

        //Set initial state
        updateStarUI(star ,ItemDAO.isFavorite(userId, itemID));

        star.setOnMouseClicked(e -> {
            ItemDAO.toggleFavorite(userId, itemID);
            updateStarUI(star, ItemDAO.isFavorite(userId, itemID));
        });

        star.setStyle("-fx-font-size: 18px; -fx-cursor: hand;");
        //Position it in your card
        this.getChildren().add(0, star);
    }

    private void updateStarUI(Label star, boolean isFav) {
        star.setText("☆");
        star.setTextFill(isFav ? javafx.scene.paint.Color.GOLD : javafx.scene.paint.Color.GRAY);
    }

    private boolean attemptBid(AuctionItem item, double newPrice) {
        if(item.placeBid(newPrice)) {
            ItemDAO.placeBidWithLog(item.getId(),SessionManager.getCurrentUser().getUser_Id(), item.getName(), newPrice);
            return true;
        }
        return false;
    }
}
