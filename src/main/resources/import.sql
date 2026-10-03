INSERT INTO rental_item (id, name, status, type)
VALUES (nextval('rental_item_seq'), 'Conference room 1', 'RENTED', 'ROOM');
INSERT INTO rental_item (id, name, status, type)
VALUES (nextval('rental_item_seq'), 'Conference room 2', 'AVAILABLE', 'ROOM');
INSERT INTO rental_item (id, name, status, type)
VALUES (nextval('rental_item_seq'), 'Dell 24 inch monitor', 'AVAILABLE', 'EQUIPMENT');
INSERT INTO rental_item (id, name, status, type)
VALUES (nextval('rental_item_seq'), 'Pool car 1', 'AVAILABLE', 'VEHICLE');

INSERT INTO reservation (id, rental_item_id, created_at, starts_at, ends_at)
SELECT nextval('reservation_seq'), id, TIMESTAMP '2026-10-01 09:00:00',
       TIMESTAMP '2026-10-05 09:00:00', TIMESTAMP '2026-10-05 17:00:00'
FROM rental_item
WHERE name = 'Conference room 1';
INSERT INTO reservation (id, rental_item_id, created_at, starts_at, ends_at)
SELECT nextval('reservation_seq'), id, TIMESTAMP '2026-10-02 11:30:00',
       TIMESTAMP '2026-10-10 10:00:00', TIMESTAMP '2026-10-10 12:00:00'
FROM rental_item
WHERE name = 'Dell 24 inch monitor';
INSERT INTO reservation (id, rental_item_id, created_at, starts_at, ends_at)
SELECT nextval('reservation_seq'), id, TIMESTAMP '2026-10-03 08:15:00',
       TIMESTAMP '2026-10-12 08:00:00', TIMESTAMP '2026-10-14 18:00:00'
FROM rental_item
WHERE name = 'Pool car 1';