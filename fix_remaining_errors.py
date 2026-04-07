#!/usr/bin/env python3
import re
import os


def fix_file(file_path):
    with open(file_path, "r") as f:
        content = f.read()

    # Fix all occurrences of SqliteUdfDataSource.COLLATION_UNICODE_3
    # More robust pattern to match any whitespace
    old_content = content

    # Pattern for .collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
    # Match any whitespace between .collate and (
    content = re.sub(
        r"\.collate\s*\(\s*SqliteUdfDataSource\.COLLATION_UNICODE_3\s*\)",
        r".apply { jooqUdfHelper.run { collateUnicode3() } }",
        content,
    )

    # Alternative: if the above doesn't work, try simpler replacement
    if old_content == content:
        # Try simpler pattern
        content = content.replace(
            "SqliteUdfDataSource.COLLATION_UNICODE_3", "jooqUdfHelper"
        )

    # Also need to handle the field before .collate
    # Actually, we need to wrap the whole expression
    # Let's do a different approach: find and replace manually

    # Write back
    with open(file_path, "w") as f:
        f.write(content)

    print(f"Processed {file_path}")
    return old_content != content


def main():
    files = [
        "/Users/duong/Documents/GitHub/komga/komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/BookSearchHelper.kt",
        "/Users/duong/Documents/GitHub/komga/komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/SeriesSearchHelper.kt",
    ]

    for file_path in files:
        fix_file(file_path)


if __name__ == "__main__":
    main()
