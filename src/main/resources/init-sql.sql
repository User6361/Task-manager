CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(255) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(55),
                       full_name VARCHAR(255)
);

CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_name VARCHAR(50) NOT NULL,
                            CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE tasks (
                       id SERIAL PRIMARY KEY,
                       title VARCHAR(255) NOT NULL,
                       status VARCHAR(50),
                       priority VARCHAR(50),
                       assignee_id BIGINT,
                       CONSTRAINT fk_assignee FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE token (
                       id SERIAL PRIMARY KEY,
                       token VARCHAR(275) NOT NULL,
                       expire_date TIMESTAMP NOT NULL,
                       expired BOOLEAN NOT NULL DEFAULT FALSE,
                       revoked BOOLEAN NOT NULL DEFAULT FALSE,
                       owner_id INTEGER,
                       CONSTRAINT fk_token_owner FOREIGN KEY (owner_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE TABLE black_list (
    id SERIAL PRIMARY KEY,

);