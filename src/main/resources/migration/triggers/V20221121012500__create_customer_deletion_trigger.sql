DROP TRIGGER IF EXISTS delete_customer;
CREATE TRIGGER delete_customer
BEFORE DELETE
ON customers FOR EACH ROW
BEGIN
    CALL delete_customer(OLD.user_id);
END;
