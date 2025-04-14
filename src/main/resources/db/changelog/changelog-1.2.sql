--liquibase formatted sql

--changeset zhavokhir:1.2.1
INSERT INTO users(name, email, password, role, created_at)
VALUES ('Ivan Ivanov', 'ivan@gmail.com', '{noop}123', 'ADMIN', current_timestamp);
--rollback delete from users where email = 'ivan@gmail.com'