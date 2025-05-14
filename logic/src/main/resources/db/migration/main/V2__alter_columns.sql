ALTER TABLE city
  ALTER COLUMN radius TYPE DOUBLE PRECISION USING radius::double precision,
  ALTER COLUMN latitude TYPE DOUBLE PRECISION USING latitude::double precision,
  ALTER COLUMN longitude TYPE DOUBLE PRECISION USING longitude::double precision;

ALTER TABLE users
  ALTER COLUMN keywords TYPE TEXT;

ALTER TABLE admin_data
  ALTER COLUMN hash TYPE VARCHAR(64),
  ALTER COLUMN salt TYPE VARCHAR(32);