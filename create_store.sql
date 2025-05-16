CREATE DATABASE IF NOT EXISTS billing;

USE billing;

CREATE TABLE customers(
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(15) NOT NULL,
    email VARCHAR(50) NOT NULL
);

CREATE TABLE products(
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock_quantity INT NOT NULL
);

CREATE TABLE bills(
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    date_time DATETIME NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    discount DECIMAL(10,2) NOT NULL
);

CREATE TABLE bill_items(
    bill_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    item_total DECIMAL(10,2) NOT NULL
);

CREATE TABLE payment(
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    customer_id INT NOT NULL,
    payment_mode VARCHAR(20) NOT NULL
);

CREATE TABLE offers(
    offer_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    dscnt_percentage DECIMAL(10,2) NOT NULL,
    valid_until DATETIME NOT NULL,
    dscrptn TEXT NOT NULL
);

