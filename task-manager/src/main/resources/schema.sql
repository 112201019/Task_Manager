-- Create Users Table First
CREATE TABLE IF NOT EXISTS users (
    user_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    username VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
    );

-- Create Tasks Table
CREATE TABLE IF NOT EXISTS tasks (
    task_id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    task_priority VARCHAR(20) NOT NULL,
    task_status VARCHAR(20) NOT NULL,
    is_recurring BOOLEAN DEFAULT FALSE,
    due_date TIMESTAMP,
    user_id UUID NOT NULL,
    CONSTRAINT fk_tasks_users FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
    );