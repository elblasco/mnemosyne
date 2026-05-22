#!/usr/bin/env bash

DB_PATH="/data/mnemosyne.db"

if [ ! -f "$DB_PATH" ]; then
  echo "Initializing database..."

  sqlite3 "$DB_PATH" <<EOF
CREATE TABLE IF NOT EXISTS users (
    hashed_username BLOB NOT NULL PRIMARY KEY,
    hashed_password BLOB NOT NULL,
    salt BLOB NOT NULL
);
CREATE TABLE IF NOT EXISTS files (
    owner BLOB NOT NULL,
    ciphertext BLOB NOT NULL,
    fileName TEXT NOT NULL,
    tag BLOB NOT NULL,
    nonce BLOB NOT NULL,
    PRIMARY KEY (owner, fileName),
    FOREIGN KEY(owner) REFERENCES users(hashed_username)
);
EOF

else
  echo "Database already exists, skipping init."
fi
