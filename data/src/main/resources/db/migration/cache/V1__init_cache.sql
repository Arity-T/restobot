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
-- Ресторан

CREATE TABLE IF NOT EXISTS restaurant (
  restaurant_id    SERIAL PRIMARY KEY, 
  tripadvisor_id   INTEGER,
  name             TEXT,
  address_string   TEXT,
  rating           NUMERIC(2, 1),
  website          TEXT,
  description      TEXT,
  latitude         DOUBLE PRECISION,
  longitude        DOUBLE PRECISION,
  city_id          INTEGER REFERENCES city(city_id),
  creating_date    TIMESTAMP DEFAULT now()
);

-- 3. Связущие (многие-ко-многим) таблицы

CREATE TABLE IF NOT EXISTS restaurant_kitchen_type (
  id              SERIAL PRIMARY KEY,
  restaurant_id   INTEGER REFERENCES restaurant(restaurant_id)     ON DELETE CASCADE,
  kitchen_type_id INTEGER REFERENCES kitchen_type(kitchen_type_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS restaurant_price_category (
  id                SERIAL PRIMARY KEY,
  restaurant_id     INTEGER REFERENCES restaurant(restaurant_id)         ON DELETE CASCADE,
  price_category_id INTEGER REFERENCES price_category(price_category_id) ON DELETE CASCADE
);