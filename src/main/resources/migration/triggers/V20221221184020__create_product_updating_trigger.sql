DROP TRIGGER IF EXISTS update_trigger;
CREATE TRIGGER update_trigger
AFTER UPDATE ON products FOR EACH ROW
BEGIN
    IF NEW.amount = 0 THEN
        UPDATE products SET available = 0 WHERE id = NEW.id;
    END IF;
    IF NEW.amount > 0 THEN
        UPDATE products SET available = 1 WHERE id = NEW.id;
    END IF;
END;