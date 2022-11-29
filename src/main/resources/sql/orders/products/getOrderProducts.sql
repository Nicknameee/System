SELECT p.id, p.name, p.price, p.description
FROM products p
INNER JOIN products_to_order pto ON p.id = pto.product_id
WHERE pto.order_id = ?;