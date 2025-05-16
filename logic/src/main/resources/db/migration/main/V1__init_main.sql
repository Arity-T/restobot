-- 1. Справочные таблицы

CREATE TABLE IF NOT EXISTS kitchen_type (
  kitchen_type_id SERIAL PRIMARY KEY,
  name            VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS city (
  city_id   SERIAL PRIMARY KEY,
  name      VARCHAR(20),
  radius    DOUBLE PRECISION,
  latitude  DOUBLE PRECISION,
  longitude DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS price_category (
  price_category_id SERIAL PRIMARY KEY,
  name              VARCHAR(25)
);

-- 2. Основная бизнес-логика
-- Пользователь

CREATE TABLE IF NOT EXISTS users (
  chat_id          BIGINT PRIMARY KEY, 
  nickname         VARCHAR(32),
  city_id          INTEGER REFERENCES city(city_id),
  keywords         TEXT
);

CREATE TABLE IF NOT EXISTS favorite_restaurant (
  favorite_restaurant_id SERIAL PRIMARY KEY,
  chat_id                BIGINT REFERENCES users(chat_id) ON DELETE CASCADE,
  tripadvisor_id         INTEGER,
  is_visited             BOOLEAN DEFAULT FALSE
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
  hash         VARCHAR(64) NOT NULL,
  salt         VARCHAR(32) NOT NULL
);