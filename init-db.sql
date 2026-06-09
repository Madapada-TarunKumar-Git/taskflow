-- Create the task schema if it doesn't exist
CREATE SCHEMA IF NOT EXISTS task;

-- Set search path to include the task schema
ALTER DATABASE taskflow_db SET search_path TO task, public;

