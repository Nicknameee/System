INSERT INTO order_history(order_number, state, event, previous_record)
VALUES(?, ?, ?, (SELECT id FROM order_history WHERE order_number = ? ORDER BY date DESC LIMIT 1));