-- 1. Справочные таблицы

CREATE TABLE IF NOT EXISTS kitchen_type (
  kitchen_type_id SERIAL PRIMARY KEY,
  name            VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS city (
  city_id   SERIAL PRIMARY KEY,
  name      VARCHAR(20),
  radius    INTEGER,
  latitude  VARCHAR(20),
  longitude VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS price_category (
  price_category_id SERIAL PRIMARY KEY,
  name              VARCHAR(25)
);

-- 2. Основная бизнес-логика

CREATE TABLE IF NOT EXISTS favorite_list (
  favorite_list_id SERIAL PRIMARY KEY,
  tripadvisor_id   INTEGER,
  is_visited       BOOLEAN DEFAULT FALSE
);

-- Пользователь

CREATE TABLE IF NOT EXISTS users (
  chat_id          BIGINT PRIMARY KEY, 
  nickname         VARCHAR(32),
  favorite_list_id INTEGER REFERENCES favorite_list(favorite_list_id),
  city_id          INTEGER REFERENCES city(city_id),
  keywords         VARCHAR(30)
);

-- 3. Связущие (многие-ко-многим) таблицы

CREATE TABLE IF NOT EXISTS user_kitchen_type (
  id              SERIAL PRIMARY KEY,
  chat_id         BIGINT REFERENCES users(chat_id)                 ON DELETE CASCADE,
  kitchen_type_id INTEGER REFERENCES kitchen_type(kitchen_type_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS user_price_category (
  id                SERIAL PRIMARY KEY,
  chat_id           BIGINT REFERENCES users(chat_id)                     ON DELETE CASCADE,
  price_category_id INTEGER REFERENCES price_category(price_category_id) ON DELETE CASCADE
);

-- 4. Админская таблица

CREATE TABLE IF NOT EXISTS admin_data (
  admin_key_id SERIAL PRIMARY KEY,
  hash         BYTEA NOT NULL,
  salt         BYTEA NOT NULL
);