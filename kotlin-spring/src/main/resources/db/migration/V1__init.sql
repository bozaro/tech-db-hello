CREATE TABLE notes
(
  description TEXT   NULL,
  completed   BOOLEAN DEFAULT FALSE,
  id          SERIAL NOT NULL
    CONSTRAINT notes_id_pk
    PRIMARY KEY
);