CREATE TABLE IF NOT EXISTS customers(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    user_id INT8 REFERENCES users(id),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    address VARCHAR(255)
)