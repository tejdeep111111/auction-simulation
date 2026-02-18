package com.auction.model;

import javafx.beans.property.*;


public class AuctionItem {
    private final int id;
    private final String name;
    private final DoubleProperty currentPrice;
    private final IntegerProperty timeLeft;
    private final String category;
    private final Object lock = new Object();

    public AuctionItem(int id, String name, double startingPrice, int duration, String category) {
        this.id = id;
        this.name = name;
        this.currentPrice = new SimpleDoubleProperty(startingPrice);
        this.timeLeft = new SimpleIntegerProperty(duration);
        this.category = category;
    }

    public boolean placeBid(double amount) {
        synchronized (this.lock) {
            if(amount > currentPrice.get()) {
                currentPrice.set(amount);
                return true;
            }
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DoubleProperty currentPriceProperty() {
        return currentPrice;
    }

    public int getTimeLeft() {
        return timeLeft.get();
    }

    public IntegerProperty timeLeftProperty() {
        return timeLeft;
    }

    public void setTimeLeft(int time) {
        this.timeLeft.set(time);
    }

}
