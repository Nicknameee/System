SELECT o.id, o.customer_id, o.order_number, o.booking_time, o.delivery_address, o.delivery_cost, o.product_cost, o.paid, o.order_status
FROM orders o
WHERE (SELECT COUNT(*) FROM orders_to_operator WHERE orders_to_operator.order_id = o.id) = 0
ORDER BY o.booking_time DESC;