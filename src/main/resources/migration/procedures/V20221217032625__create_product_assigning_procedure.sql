DROP PROCEDURE IF EXISTS assign_product;
CREATE PROCEDURE assign_product(IN order_id_var INT8, IN product_id_var INT8)
BEGIN
    IF (SELECT COUNT(*) FROM products WHERE products.id = product_id_var AND products.available = 1 AND products.amount > 0) > 0
    THEN
        INSERT INTO products_to_order(product_id, order_id) VALUES(product_id_var, order_id_var);
        UPDATE products SET amount = amount - 1 WHERE id = product_id_var;
    END IF;
END;