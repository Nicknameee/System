SELECT p.id, p.name, p.price, p.amount, p.available, p.description
FROM products p
WHERE p.id = ?;