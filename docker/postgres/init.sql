-- PostgreSQL initialization script for Komga
-- Creates necessary extensions and sets up database

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search/pattern matching
CREATE EXTENSION IF NOT EXISTS "unaccent"; -- For accent removal (similar to UDF_STRIP_ACCENTS)

-- Set reasonable default configuration for Komga
ALTER DATABASE komga SET timezone TO 'UTC';
ALTER DATABASE komga SET default_transaction_isolation TO 'read committed';
ALTER DATABASE komga SET lock_timeout TO '30s';
ALTER DATABASE komga SET statement_timeout TO '60000';

-- Create schema if needed (public schema is default)