INSERT INTO orders(customer_id, order_number, delivery_address, delivery_cost, product_cost, paid, order_status) VALUES(?, ?, ?, ?, ?, ?, ?);
SELECT LAST_INSERT_ID();