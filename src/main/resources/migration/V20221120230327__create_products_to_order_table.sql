CREATE TABLE IF NOT EXISTS products_to_order(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    product_id INT8 REFERENCES products(id),
    order_id INT8 REFERENCES orders(id)
)