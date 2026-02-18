package com.auction.ui;

import com.auction.Main;
import com.auction.database.ItemDAO;
import com.auction.model.AuctionItem;
import com.auction.util.SessionManager;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;


public class MainDashboard {
    private FlowPane cardContainer;

    public void show(Stage stage) {
        BorderPane root = new BorderPane();

        //1- TOP HEADER
        HBox header = new HBox();
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        Label appName = new Label("AUCTION PRO ⏱");
        appName.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: Black;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);


        header.getChildren().addAll(appName, spacer);

        //2- NAVIGATION PANEL LEFT
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(20));
        //
        nav.setStyle("-fx-background-color: #404040; -fx-pref-width: 200;");

        Label navLabel = new Label("CATEGORIES");
        navLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #ffffff; -fx-font-weight: bold;");

        nav.getChildren().add(navLabel);

        String[] categories = {"All", "Electronics", "Vintage", "Art"};
        for (String cat : categories) {
            Button btn = new Button(cat);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-cursor: hand;");
            btn.setOnAction(e -> refreshItems(cat));
            nav.getChildren().add(btn);
        }

        //3- CENTRE CONTENT
        cardContainer = new FlowPane(20, 20);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);

        root.setTop(header);
        root.setLeft(nav);
        root.setCenter(scrollPane);

        //Admin functionality to add items
        if(SessionManager.getCurrentUser().getRole().equals("admin")) {
            Button addButton = new Button("+ Add Item");
            addButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
            addButton.setOnAction(e -> showAddItemDialog());
            header.getChildren().add(1, addButton);
        }

        refreshItems("All");
        stage.setScene(new Scene(root, 1100, 750));
    }

    private void refreshItems(String category) {
        cardContainer.getChildren().clear();
        List<AuctionItem> items = ItemDAO.getItemsByCategory(category);
        for(AuctionItem item : items) {
            cardContainer.getChildren().add(new AuctionCard(item));
            startAuctionTimer(item);
        }

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

    //Dialog box to add an item to the list by admin
    private void showAddItemDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Auction Item");

        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        TextField nameInput = new TextField();
        nameInput.setPromptText("Item Name");
        TextField priceInput = new TextField();
        priceInput.setPromptText("Starting Price");
        TextField timeInput = new TextField();
        timeInput.setPromptText("Duration (seconds)");

        ComboBox<String> categoryList = new ComboBox<>();
        categoryList.getItems().addAll("Electronics", "Vintage", "Art");
        categoryList.setPromptText("Select Category");

        Button saveButton = new Button("Save to DB");
        saveButton.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white;");

        saveButton.setOnAction(e -> {
            try {
                String name = nameInput.getText();
                double price = Double.parseDouble(priceInput.getText());
                int time = Integer.parseInt(timeInput.getText());
                String category = categoryList.getValue();

                if(ItemDAO.addItem(name, price, time, category)) {
                    dialog.close();
                    refreshItems("All");
                }
            } catch (NumberFormatException numberFormatException) {
                Main.showToast("Invalid number format!!");
            }
        });

        form.getChildren().addAll(new Label("Add New Item"), nameInput, priceInput, timeInput, categoryList, saveButton);
        dialog.setScene(new Scene(form, 300, 400));
        dialog.show();
    }

}
