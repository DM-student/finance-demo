CREATE TABLE users (
        id INT GENERATED BY DEFAULT AS IDENTITY,
        PRIMARY KEY (id),
        login VARCHAR(328) NOT NULL,
        password VARCHAR(64) NOT NULL,
        login_token TEXT,
        status TEXT,
        status_reason TEXT
);