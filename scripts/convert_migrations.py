#!/usr/bin/env python3
"""
Script to convert SQLite migrations to PostgreSQL migrations.
"""

import os
import re
from pathlib import Path


def convert_sqlite_to_postgresql(sql_content):
    """Convert SQLite SQL to PostgreSQL SQL."""

    # Replace datetime with timestamp
    sql_content = re.sub(r"\bdatetime\b", "timestamp", sql_content, flags=re.IGNORECASE)

    # Replace boolean defaults 0/1 with false/true
    sql_content = re.sub(
        r"DEFAULT\s+0\b", "DEFAULT false", sql_content, flags=re.IGNORECASE
    )
    sql_content = re.sub(
        r"DEFAULT\s+1\b", "DEFAULT true", sql_content, flags=re.IGNORECASE
    )

    # Replace int8 with bigint
    sql_content = re.sub(r"\bint8\b", "bigint", sql_content, flags=re.IGNORECASE)

    # Replace blob with bytea
    sql_content = re.sub(r"\bblob\b", "bytea", sql_content, flags=re.IGNORECASE)

    # Quote reserved keywords (USER is the main one)
    sql_content = re.sub(r"\bUSER\b", '"USER"', sql_content)

    # Handle CREATE TABLE syntax differences
    # SQLite uses CURRENT_TIMESTAMP, PostgreSQL uses CURRENT_TIMESTAMP (same)
    # But we need to ensure timestamp vs datetime

    # Handle ALTER TABLE ADD COLUMN - PostgreSQL doesn't need COLUMN keyword
    # Actually both support it, but we'll keep it

    # Handle CREATE INDEX IF NOT EXISTS - PostgreSQL 9.5+ supports it

    # Handle INSERT statements - mostly the same

    # Handle UPDATE statements - mostly the same

    return sql_content


def process_sql_migration(sqlite_path, postgresql_path):
    """Process a single SQL migration file."""
    print(f"Processing: {sqlite_path}")

    with open(sqlite_path, "r") as f:
        sql_content = f.read()

    # Convert the SQL
    postgresql_sql = convert_sqlite_to_postgresql(sql_content)

    # Write to PostgreSQL directory
    with open(postgresql_path, "w") as f:
        f.write(postgresql_sql)

    print(f"  -> Written to: {postgresql_path}")


def analyze_kotlin_migration(kotlin_path):
    """Analyze a Kotlin migration to understand what needs to be converted."""
    print(f"Analyzing Kotlin migration: {kotlin_path}")

    with open(kotlin_path, "r") as f:
        content = f.read()

    # Check for SQL queries in the Kotlin file
    sql_queries = re.findall(r"\"\"\"([\s\S]*?)\"\"\"", content)
    sql_queries.extend(re.findall(r"\"([\s\S]*?)\"", content))

    # Filter for likely SQL queries
    sql_keywords = ["SELECT", "INSERT", "UPDATE", "DELETE", "CREATE", "ALTER", "DROP"]
    for query in sql_queries:
        if (
            any(keyword in query.upper() for keyword in sql_keywords)
            and len(query) > 20
        ):
            print(f"  Found SQL query: {query[:100]}...")

    return content


def main():
    base_dir = Path("komga/src/flyway/resources/db/migration")
    sqlite_dir = base_dir / "sqlite"
    postgresql_dir = base_dir / "postgresql"

    # Create PostgreSQL directory if it doesn't exist
    postgresql_dir.mkdir(parents=True, exist_ok=True)

    # Process SQL migrations
    sql_files = list(sqlite_dir.glob("*.sql"))
    print(f"Found {len(sql_files)} SQL migration files")

    converted_count = 0
    for sqlite_file in sql_files:
        postgresql_file = postgresql_dir / sqlite_file.name

        # Skip if already exists (initial migration already converted)
        if postgresql_file.exists():
            print(f"Skipping (already exists): {sqlite_file.name}")
            converted_count += 1
            continue

        process_sql_migration(sqlite_file, postgresql_file)
        converted_count += 1

    print(f"\nConverted {converted_count} SQL migration files")

    # Analyze Kotlin migrations
    kotlin_base_dir = Path("komga/src/flyway/kotlin/db/migration")
    kotlin_sqlite_dir = kotlin_base_dir / "sqlite"

    if kotlin_sqlite_dir.exists():
        kotlin_files = list(kotlin_sqlite_dir.glob("*.kt"))
        print(f"\nFound {len(kotlin_files)} Kotlin migration files")

        for kotlin_file in kotlin_files:
            analyze_kotlin_migration(kotlin_file)

    print("\nConversion complete!")
    print("\nNext steps:")
    print("1. Review converted migrations for any manual fixes needed")
    print("2. Create PostgreSQL versions of Kotlin migrations")
    print("3. Test migrations with PostgreSQL Testcontainers")


if __name__ == "__main__":
    main()
