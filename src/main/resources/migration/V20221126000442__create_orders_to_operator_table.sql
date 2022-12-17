CREATE TABLE IF NOT EXISTS orders_to_operator(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    operator_id INT8 REFERENCES users(id),
    order_id INT8 REFERENCES orders(id),
    UNIQUE(order_id)
)