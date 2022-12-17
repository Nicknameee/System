DELETE FROM products_to_order WHERE order_id = <orderId>
<if(productId)>
AND product_id = <productId> LIMIT 1
<endif>