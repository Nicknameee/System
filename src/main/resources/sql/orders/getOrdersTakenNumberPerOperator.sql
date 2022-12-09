SELECT u.id, u.username, (SELECT COUNT(*) FROM orders_to_operator WHERE operator_id = u.id) AS orders_taken_number
FROM users u
WHERE u.role = 1
ORDER BY orders_taken_number ASC;