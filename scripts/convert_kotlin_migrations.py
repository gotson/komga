#!/usr/bin/env python3
"""
Script to create PostgreSQL versions of Kotlin migrations.
"""

import os
import re
from pathlib import Path


def convert_kotlin_migration(kotlin_content):
    """Convert Kotlin migration for PostgreSQL."""

    # Change package from sqlite to postgresql
    kotlin_content = kotlin_content.replace(
        "package db.migration.sqlite", "package db.migration.postgresql"
    )

    # Change class name if needed (optional, but good for clarity)
    # Actually keep same name since Flyway uses version number

    # Check for any SQLite-specific SQL that needs conversion
    # Most SQL in Kotlin migrations should be standard SQL

    return kotlin_content


def main():
    sqlite_kotlin_dir = Path("komga/src/flyway/kotlin/db/migration/sqlite")
    postgresql_kotlin_dir = Path("komga/src/flyway/kotlin/db/migration/postgresql")

    # Create PostgreSQL directory if it doesn't exist
    postgresql_kotlin_dir.mkdir(parents=True, exist_ok=True)

    # Process Kotlin migrations
    kotlin_files = list(sqlite_kotlin_dir.glob("*.kt"))
    print(f"Found {len(kotlin_files)} Kotlin migration files")

    for kotlin_file in kotlin_files:
        print(f"Processing: {kotlin_file.name}")

        with open(kotlin_file, "r") as f:
            kotlin_content = f.read()

        # Convert for PostgreSQL
        postgresql_content = convert_kotlin_migration(kotlin_content)

        # Write to PostgreSQL directory
        postgresql_file = postgresql_kotlin_dir / kotlin_file.name
        with open(postgresql_file, "w") as f:
            f.write(postgresql_content)

        print(f"  -> Written to: {postgresql_file}")

    print("\nKotlin migration conversion complete!")
    print(
        "\nNote: Review the converted files for any SQLite-specific SQL that needs manual adjustment."
    )


if __name__ == "__main__":
    main()
