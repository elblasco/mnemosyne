#!/usr/bin/env bash

DB_PATH="/data/mnemosyne.db"

if [ ! -f "$DB_PATH" ]; then
  echo "Initializing database..."

  admin_salt=$(date +%s)
  admin_hash=$(echo -n "adminpass$admin_salt" | cksum -a sha256 | awk '{print $4}')
  user1_salt=$(date +%s)
  user1_hash=$(echo -n "user1pass$user1_salt" | cksum -a sha256 | awk '{print $4}')

  sqlite3 "$DB_PATH" <<EOF
CREATE TABLE IF NOT EXISTS users (
    username TEXT NOT NULL PRIMARY KEY,
    hashed_password TEXT NOT NULL,
    salt TEXT NOT NULL
);
INSERT INTO users (username, hashed_password, salt) VALUES
    ('admin', "$admin_hash", "$admin_salt"),
    ('user1', "$user1_hash", "$user1_salt");
CREATE TABLE IF NOT EXISTS files (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    owner TEXT NOT NULL,
    ciphertext BLOB NOT NULL,
    encryptedFileName TEXT NOT NULL,
    tag TEXT NOT NULL,
    nonce TEXT NOT NULL,
    FOREIGN KEY(owner) REFERENCES users(username)
);
EOF

else
  echo "Database already exists, skipping init."
fi