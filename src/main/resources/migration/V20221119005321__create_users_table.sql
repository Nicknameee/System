CREATE TABLE IF NOT EXISTS users(
    id INT8 PRIMARY KEY NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    login_time DATETIME NOT NULL DEFAULT NOW(),
    logout_time DATETIME NOT NULL DEFAULT '1900-01-01 00:00:00',
    role INT8 NOT NULL,
    status INT8 NOT NULL,
    timezone VARCHAR(255) NOT NULL
);