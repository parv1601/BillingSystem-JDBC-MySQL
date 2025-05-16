ALTER TABLE bills
ADD foreign key (customer_id) references customers(customer_id);

ALTER TABLE bill_items
ADD foreign key (bill_id) references bills(bill_id),
ADD foreign key (product_id) references products(product_id);

ALTER TABLE payment
ADD foreign key (bill_id) references bills(bill_id),
ADD foreign key (customer_id) references customers(customer_id);

ALTER TABLE offers
ADD foreign key (product_id) references products(product_id);

