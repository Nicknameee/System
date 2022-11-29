CREATE TABLE IF NOT EXISTS order_history(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    order_number VARCHAR(255) NOT NULL,
    state JSON,
    event INT8 NOT NULL,
    previous_record INT8,
    date TIMESTAMP DEFAULT NOW()
)