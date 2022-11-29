DROP PROCEDURE IF EXISTS delete_order;
CREATE PROCEDURE delete_order(IN order_id INT8, IN order_number INT8)
BEGIN
    DECLARE previous_history_record_id_var INT8;
    SELECT id INTO previous_history_record_id_var FROM order_history WHERE order_history.order_number = order_number ORDER BY date DESC LIMIT 1;
    INSERT INTO order_history(order_number, state, event, previous_record) VALUES(order_number, null, 3, previous_history_record_id_var);
    DELETE FROM products_to_order WHERE products_to_order.order_id = order_id;
END;