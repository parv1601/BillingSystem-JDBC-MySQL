INSERT INTO customers (name, phone, email) VALUES
('Alice Sharma', '9876543210', 'alice@example.com'),
('Rohit Mehra', '9123456780', 'rohit@example.com'),
('Kavya Reddy', '9988776655', 'kavya@example.com');

INSERT INTO products (name, price, stock_quantity) VALUES
('Dove Soap', 45.00, 100),
('Parle-G Biscuits', 10.00, 200),
('Colgate Toothpaste', 90.00, 50),
('Kellogg’s Cornflakes', 150.00, 30);

INSERT INTO bills (customer_id, date_time, total_amount, discount) VALUES
(1, NOW(), 145.00, 5.00),
(2, NOW(), 160.00, 10.00);

INSERT INTO bill_items (bill_id, product_id, quantity, item_total) VALUES
(1, 1, 2, 90.00),   -- Dove Soap x2
(1, 2, 1, 10.00),   -- Parle-G x1
(2, 3, 1, 90.00),   -- Colgate x1
(2, 4, 1, 150.00);  -- Cornflakes x1

INSERT INTO payment (bill_id, customer_id, payment_mode) VALUES
(1, 1, 'UPI'),
(2, 2, 'Cash');

INSERT INTO offers (product_id, dscnt_percentage, valid_until, dscrptn) VALUES
(1, 10.00, '2025-12-31 23:59:59', '10% off on Dove Soap'),
(4, 15.00, '2025-05-15 23:59:59', 'Summer offer on Cornflakes');

