SELECT o.id, o.order_number, o.booking_time, o.delivery_address, o.delivery_cost, o.product_cost, o.paid, o.order_status
FROM orders o
INNER JOIN orders_to_operator oto ON oto.order_id = o.id
WHERE oto.operator_id = ?
ORDER BY o.booking_time DESC;