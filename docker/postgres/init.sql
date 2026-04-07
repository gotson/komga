-- PostgreSQL initialization script for Komga
-- Creates necessary extensions and sets up database

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm"; -- For text search/pattern matching
CREATE EXTENSION IF NOT EXISTS "unaccent"; -- For accent removal (similar to UDF_STRIP_ACCENTS)