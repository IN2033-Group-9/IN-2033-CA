# coding=utf-8
from faker import Faker
import random
import os

fake = Faker('en_GB')

print("Saving to:", os.getcwd())

def esc(val):
    return str(val).replace("'", "''")

rows = []

for i in range(1, 1001):
    username = esc(fake.user_name())
    password_hash = fake.sha256()
    role_id = random.randint(1, 3)

    created_at = fake.date_time_between(
        start_date='-2y',
        end_date='now'
    ).strftime('%Y-%m-%d %H:%M:%S')

    sql_line = (
        "INSERT INTO ca_users (user_id, username, password_hash, role_id, created_at) "
        f"VALUES ({i}, '{username}', '{password_hash}', {role_id}, '{created_at}');"
    )

    rows.append(sql_line)

# FORCE Desktop save
file_path = "/Users/ben/Desktop/users_insert.sql"

with open(file_path, "w") as f:
    f.write("\n".join(rows))

print("✅ File saved at:", file_path)