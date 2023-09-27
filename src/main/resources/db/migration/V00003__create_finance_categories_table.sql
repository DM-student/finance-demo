CREATE TABLE finance_categories (
        id INT GENERATED BY DEFAULT AS IDENTITY,
        PRIMARY KEY (id),
        owner_id INT REFERENCES users(id) ON DELETE CASCADE,
        title VARCHAR(64) NOT NULL
);