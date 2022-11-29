CREATE TABLE IF NOT EXISTS products(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL UNIQUE,
    price DOUBLE CHECK(price >= 0) DEFAULT 0,
    amount INT8 DEFAULT 0 CHECK(amount >= 0),
    available BOOLEAN DEFAULT FALSE,
    description JSON
)