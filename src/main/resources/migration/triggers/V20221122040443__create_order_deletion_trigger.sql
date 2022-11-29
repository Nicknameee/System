DROP TRIGGER IF EXISTS delete_order;
CREATE TRIGGER delete_order
BEFORE DELETE ON orders FOR EACH ROW
BEGIN
    CALL delete_order(OLD.id, OLD.order_number);
END;