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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.image.*;

import java.util.ArrayList;
import java.util.List;


public class MainDashboard {
    private FlowPane cardContainer;
    private final List<Thread> activeTimers = new ArrayList<>();

    public void show(Stage stage) {
        BorderPane root = new BorderPane();

        //1- TOP HEADER
        HBox header = new HBox(20);
        header.setPadding(new Insets(15, 25, 15, 25));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

        Label homeLabel = new Label("HOME");
        homeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: Black;");

        Label usernameLabel = new Label(SessionManager.getCurrentUser().getUsername());
        usernameLabel.setStyle("-fx-font-size: 13px");

        //Admin tools
        HBox adminToolBar = new HBox(10);
        adminToolBar.setAlignment(Pos.CENTER_LEFT);
        //Admin functionality to add items
        if(SessionManager.getCurrentUser().getRole().equals("admin")) {
            Button addButton = new Button("+ Add Item");
            addButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
            addButton.setOnAction(e -> showAddItemDialog());

            Button bidLogsButton = new Button("Bid logs");
            bidLogsButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
            bidLogsButton.setOnAction(e -> showBidLogsDialog());
            adminToolBar.getChildren().addAll(addButton, bidLogsButton);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        //Log out button
        Button logoutButton = new Button("Log Out ->");
        logoutButton.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-weight: bold; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> {
            showLoginScreen();
        });

        //Logo Icon at the far right
        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/login-view-logo.png")));
            logoView.setFitHeight(40);
            logoView.setPreserveRatio(true);
            header.getChildren().addAll(homeLabel, adminToolBar, spacer, usernameLabel, logoutButton, logoView);
        } catch(Exception e) {
            header.getChildren().addAll(homeLabel, adminToolBar, usernameLabel, logoutButton, spacer);
        }

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
        Button favsBtn = new Button("My Favorites");
        favsBtn.setStyle("-fx-font-size: 16px; -fx-background-color: transparent; -fx-text-fill: #FFD700; -fx-cursor: hand; -fx-font-weight: bold;");
        favsBtn.setOnAction(e -> showFavorites());
        nav.getChildren().add(favsBtn);

        //3- CENTRE CONTENT
        cardContainer = new FlowPane(20, 20);
        cardContainer.setPadding(new Insets(20));
        cardContainer.setAlignment(Pos.TOP_CENTER);

        ScrollPane scrollPane = new ScrollPane(cardContainer);
        scrollPane.setFitToWidth(true);

        root.setTop(header);
        root.setLeft(nav);
        root.setCenter(scrollPane);

        refreshItems("All");
        stage.setScene(new Scene(root, 1100, 750));
    }

    private void refreshItems(String category) {
        stopAllTimers();
        cardContainer.getChildren().clear();
        List<AuctionItem> items = ItemDAO.getItemsByCategory(category);
        for(AuctionItem item : items) {
            cardContainer.getChildren().add(new AuctionCard(item));
            startAuctionTimer(item);
        }

    }

    //Causes each AuctionItem to spawn a single thread
    private void startAuctionTimer(AuctionItem item) {
        Thread timerThread = new Thread(() -> {
            while(item.getTimeLeft() > 0) {
                try {
                    Thread.sleep(1000); //1second
                    double latestPrice = ItemDAO.getCurrentPriceFromDB(item.getId());
                    Platform.runLater(() -> {
                        item.setTimeLeft(item.getTimeLeft() - 1);
                        // Sync price from DB so all instances see real-time updates
                        if (latestPrice > item.currentPriceProperty().get()) {
                            item.currentPriceProperty().set(latestPrice);
                        }
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // restore interrupted flag and exit cleanly
                    return;
                }
            }
        });
        timerThread.setDaemon(true);
        timerThread.start();
        activeTimers.add(timerThread);
    }

    // Interrupts and clears all active timer threads to free their resources
    private void stopAllTimers() {
        for (Thread t : activeTimers) {
            t.interrupt();
        }
        activeTimers.clear();
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

    private void showBidLogsDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Global Bid logs");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        ListView<String> listView = new ListView<>();
        listView.getItems().addAll(ItemDAO.getBidLogs());
        listView.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 13px;");

        Label title = new Label("Audit Trail: All Bidding Activity");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px");

        root.getChildren().addAll(title, listView);
        dialog.setScene(new Scene(root, 500, 400));
        dialog.show();
    }

    private void showFavorites() {
        stopAllTimers();
        cardContainer.getChildren().clear();
        int userId = SessionManager.getCurrentUser().getUser_Id();
        List<AuctionItem> favoriteItems = ItemDAO.getFavoriteItems(userId);

        for (AuctionItem item : favoriteItems) {
            cardContainer.getChildren().add(new AuctionCard(item));
        }
    }

    private void showLoginScreen() {
        try {
            stopAllTimers(); // stop all running timers before leaving the dashboard
            SessionManager.logout();

            Stage stage = (Stage) cardContainer.getScene().getWindow();

            LoginView loginView = new LoginView();
            loginView.show(stage, () -> {
                // This code runs ONLY after the user logs in successfully again
                MainDashboard freshDashboard = new MainDashboard();
                freshDashboard.show(stage);});
        } catch (Exception e) {
            System.err.println("Failed to transition to login screen: " + e.getMessage());
        }
    }
}
