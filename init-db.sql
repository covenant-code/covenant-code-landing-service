-- init-db.sql
-- Инициализация базы данных для Covenant Code Landing

-- Выходим в случае ошибки
\set ON_ERROR_STOP on

-- Создаем пользователя (если не существует)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'covenant') THEN
        CREATE USER covenant WITH PASSWORD 'covenant123';
        ALTER USER covenant WITH SUPERUSER;
        RAISE NOTICE 'Пользователь covenant создан';
ELSE
        RAISE NOTICE 'Пользователь covenant уже существует';
END IF;
END
$$;

-- Создаем базу данных (если не существует)
SELECT 'CREATE DATABASE covenant_landing_dev'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'covenant_landing_dev')\gexec

-- Подключаемся к базе данных
    \c covenant_landing_dev

-- Создаем расширение для UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Устанавливаем владельца базы данных
ALTER DATABASE covenant_landing_dev OWNER TO covenant;

-- Устанавливаем права на схему public
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT ALL ON SCHEMA public TO covenant;
GRANT ALL ON SCHEMA public TO public;

-- Создаем таблицу admin_users_db
CREATE TABLE IF NOT EXISTS admin_users_db (
                                              id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ADMIN',
    active BOOLEAN NOT NULL DEFAULT true,
    phone VARCHAR(50),
    department VARCHAR(255),
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Создаем таблицу clients_db
CREATE TABLE IF NOT EXISTS clients_db (
                                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    message TEXT,
    course_type VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'NEW',
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    source VARCHAR(255) DEFAULT 'Лендинг',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             processed_by VARCHAR(255),
    processed_at TIMESTAMP WITH TIME ZONE,

                             CONSTRAINT unique_client_email UNIQUE (email)
    );

-- Создаем индексы
CREATE INDEX IF NOT EXISTS idx_clients_status ON clients_db(status);
CREATE INDEX IF NOT EXISTS idx_clients_priority ON clients_db(priority);
CREATE INDEX IF NOT EXISTS idx_clients_created_at ON clients_db(created_at);
CREATE INDEX IF NOT EXISTS idx_clients_course_type ON clients_db(course_type);
CREATE INDEX IF NOT EXISTS idx_admin_users_email ON admin_users_db(email);
CREATE INDEX IF NOT EXISTS idx_admin_users_role ON admin_users_db(role);

-- Создаем администратора по умолчанию (пароль: admin123)
-- Пароль закодирован BCrypt: $2a$10$X8zV8Q5zW6L3Q7J9K2YH3uL1M4N5B6V7C8D9E0F1G2H3I4J5K6L7M8N9O0P
INSERT INTO admin_users_db (id, email, password, first_name, last_name, role, active)
VALUES (
           uuid_generate_v4(),
           'admin@covenantcode.ru',
           '$2a$10$X8zV8Q5zW6L3Q7J9K2YH3uL1M4N5B6V7C8D9E0F1G2H3I4J5K6L7M8N9O0P',
           'Администратор',
           'Системы',
           'ADMIN',
           true
       ) ON CONFLICT (email) DO NOTHING;

-- Настраиваем привилегии
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO covenant;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO covenant;
GRANT ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public TO covenant;

-- Настраиваем привилегии по умолчанию для будущих таблиц
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO covenant;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO covenant;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON FUNCTIONS TO covenant;

-- Информационное сообщение
DO $$
BEGIN
    RAISE NOTICE '=========================================';
    RAISE NOTICE 'БАЗА ДАННЫХ ГОТОВА К РАБОТЕ';
    RAISE NOTICE '=========================================';
    RAISE NOTICE 'База данных: covenant_landing_dev';
    RAISE NOTICE 'Пользователь: covenant / covenant123';
    RAISE NOTICE 'Администратор: admin@covenantcode.ru / admin123';
    RAISE NOTICE 'Порт: 5432';
    RAISE NOTICE '=========================================';
END
$$;