DELETE FROM products_to_order WHERE order_id = <orderId>
<if(productIds)>
AND product_id IN (<productIds>)
<endif>