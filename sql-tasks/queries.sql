SELECT p.*
FROM properties p
LEFT JOIN bookings b ON p.id = b.property_id
WHERE b.id IS NULL;


SELECT b1.id AS booking1_id, b2.id AS booking2_id
FROM bookings b1
JOIN bookings b2 ON b1.property_id = b2.property_id AND b1.id < b2.id
WHERE b1.start_date < b2.end_date AND b2.start_date < b1.end_date;


SELECT u.id, u.name, COUNT(DISTINCT b.property_id) AS properties_booked
FROM users u
JOIN bookings b ON u.id = b.user_id
GROUP BY u.id, u.name
ORDER BY properties_booked DESC
LIMIT 5;


Recommendations:
1 - use Explain to identify slow queries.
2 - Create index on bookings.property_id column.
3 - Create index on bookings.user_id column.
4 - Create index on properties.location column.
