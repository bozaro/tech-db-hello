-- +migrate Up
CREATE TABLE tasks (
  id          BIGSERIAL PRIMARY KEY,
  description TEXT,
  completed   BOOL
);
