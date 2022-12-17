SELECT o.id, o.customer_id, o.order_number, o.booking_time, o.delivery_address, o.delivery_cost, o.product_cost, o.paid, o.order_status
FROM orders o
<if(productIds)>
INNER JOIN products_to_order pto ON pto.order_id = o.id
<endif>
WHERE true
<if(productIds)>
AND pto.product_id IN (<productIds>)
<endif>
<if(orderNumbers)>
AND o.order_number IN (<orderNumbers>)
<endif>
<if(orderStatuses)>
AND o.order_status IN (<orderStatuses>)
<endif>
<if(bookingTimeBottom && bookingTimeTop)>
AND o.booking_time BETWEEN '<bookingTimeBottom>' AND '<bookingTimeTop>'
<elseif(bookingTimeBottom && !bookingTimeTop)>
AND o.booking_time >= '<bookingTimeBottom>'
<elseif(!bookingTimeBottom && bookingTimeTop)>
AND o.booking_time \<= '<bookingTimeTop>'
<endif>
<if(!paid)>
AND o.paid = 0
<else>
AND o.paid = 1
<endif>
<if(costBottom && costTop)>
AND o.total_cost BETWEEN <costBottom> AND <costTop>
<elseif(costBottom && !costTop)>
AND o.total_cost >= <costBottom>
<elseif(!costBottom && costTop)>
AND o.total_cost \<= <costTop>
<endif>
ORDER BY o.booking_time DESC;