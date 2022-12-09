DELETE FROM orders_to_operator
WHERE operator_id = ?
<if(orderIds)>
AND order_id IN (<orderIds>)
<endif>