SELECT u.id as user_id, u.username, u.password, u.login_time, u.logout_time,
u.role, u.status, u.timezone, c.id, c.first_name, c.last_name, c.address
FROM customers c
INNER JOIN users u ON c.user_id = u.id
WHERE c.id = ?;