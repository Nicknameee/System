SELECT node.id, node.order_number, node.state, node.event, node.previous_record, node.date
FROM order_history
WHERE order_number = ?
ORDER BY node.date DESC;