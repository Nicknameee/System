SELECT o.id, o.order_number, o.booking_time, o.delivery_address, o.delivery_cost, o.product_cost, o.paid, o.order_status
FROM orders o
WHERE o.id = ?;