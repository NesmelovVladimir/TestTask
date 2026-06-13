-- Таблица пользователей
CREATE TABLE users (
                       id uuid PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL
);

-- Таблица задач
CREATE TABLE tasks (
                       id VARCHAR(8) PRIMARY KEY,
                       user_id uuid NOT NULL,
                       status VARCHAR(20),
                       payload VARCHAR(1000),
                       result VARCHAR(4000),
                       progress INT DEFAULT 0,
                       created_at TIMESTAMP,
                       completed_at TIMESTAMP,
                       FOREIGN KEY (user_id) REFERENCES users(id)
);

insert
into
    public.users (id,
                  "password",
                  username)
values('4629e7c3-38cb-4b6d-b857-635f50ec21d2', '$2a$10$y7RrKJKjkF0fm8433TS12OOp6YcXaJ0WCeUn5CtdHuzlMzqikomCG', 'user');

insert
into
    public.users (id,
                  "password",
                  username)
values('ad235e62-947f-4007-8fde-d310146c707b', '$2a$10$O4bu9B9nvc13CLF3fg3DRO6LcGgMLTH19JM46Nophqq27j24SV0x6', 'admin');

CREATE INDEX idx_tasks_status_created ON tasks(status, created_at);
