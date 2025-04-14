--liquibase formatted sql

--changeset zhavokhir:1.1
CREATE TABLE users(
    id SERIAL PRIMARY KEY ,
    name VARCHAR(255) NOT NULL ,
    email VARCHAR(255) NOT NULL UNIQUE ,
    password VARCHAR(255) NOT NULL DEFAULT '{noop}123',
    role VARCHAR(32) NOT NULL,
    created_at TIMESTAMP default CURRENT_TIMESTAMP
);
--rollback drop table if exists users

--changeset zhavokhir:1.2
CREATE TABLE tokens(
    id SERIAL PRIMARY KEY ,
    user_id INTEGER REFERENCES users(id) ON DELETE CASCADE NOT NULL ,
    refresh_token VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
)
--rollback drop table if exists tokens