-- +migrate Up
CREATE TABLE tasks (
  id          INTEGER PRIMARY KEY AUTOINCREMENT,
  description TEXT,
  completed   BOOL
);
