package com.auction.model;

import javafx.beans.property.*;


public class AuctionItem {
    private final String name;
    private DoubleProperty currentPrice;
    private IntegerProperty timeLeft;
    private final Object lock = new Object();

    public AuctionItem(String name, double startingPrice, int duration) {
        this.name = name;
        this.currentPrice = new SimpleDoubleProperty(startingPrice);
        this.timeLeft = new SimpleIntegerProperty(duration);
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
