CREATE TABLE IF NOT EXISTS orders(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    customer_id INT8 REFERENCES customers(id),
    order_number VARCHAR(255) NOT NULL UNIQUE,
    booking_time TIMESTAMP DEFAULT NOW(),
    delivery_address VARCHAR(255),
    delivery_cost DOUBLE CHECK(delivery_cost >= 0),
    product_cost DOUBLE CHECK(product_cost >= 0),
    paid BOOLEAN DEFAULT FALSE,
    order_status INT8 NOT NULL
)