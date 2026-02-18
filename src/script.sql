-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS AuctionSimulation;

USE AuctionSimulation;

-- 2. Create Users Table (for Authentication & RBAC)
CREATE TABLE IF NOT EXISTS Users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('admin', 'user') DEFAULT 'user'
);

-- 3. Create Items Table (with Category Support)
CREATE TABLE IF NOT EXISTS Items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    current_price DOUBLE NOT NULL,
    time_left INT NOT NULL,
    category VARCHAR(50) NOT NULL
);

-- 4. Create BidLogs Table (Audit Trail)
-- This table links Users and Items using Foreign Keys
CREATE TABLE IF NOT EXISTS BidLogs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    item_id INT NOT NULL,
    user_id INT NOT NULL,
    bid_amount DOUBLE NOT NULL,
    bid_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES Items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- 5. Seed Initial Data
-- Default Credentials
INSERT INTO Users (username, password, role) VALUES
('admin', 'admin123', 'admin'),
('user', 'user123', 'user');

-- Default Auction Items
INSERT INTO Items (name, current_price, time_left, category) VALUES
('Electric Guitar', 1850.00, 300, 'Electronics'),
('Rare Coin', 5553.00, 174, 'Vintage'),
('Mechanical Keyboard', 250.00, 44, 'Electronics'),
('Drawing', 812.00, 639, 'Art'),
('High-End Drone', 1590.00, 174, 'Electronics'),
('Typewriter (1950s)', 740.00, 294, 'Vintage');