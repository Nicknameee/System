DROP PROCEDURE IF EXISTS delete_customer;
CREATE PROCEDURE delete_customer(IN user_id INT8)
BEGIN
    DELETE FROM orders WHERE customer_id = user_id;
    DELETE FROM users WHERE id = user_id;
END;