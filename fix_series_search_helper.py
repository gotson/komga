#!/usr/bin/env python3
import re


def fix_series_search_helper():
    file_path = "/Users/duong/Documents/GitHub/komga/komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/SeriesSearchHelper.kt"

    with open(file_path, "r") as f:
        content = f.read()

    # Fix collate(SqliteUdfDataSource.COLLATION_UNICODE_3) calls
    # Pattern: .collate(SqliteUdfDataSource.COLLATION_UNICODE_3) -> jooqUdfHelper.run { .collateUnicode3() }
    # Need to handle the field before .collate

    # This is complex because we need to capture the field expression
    # Let's do a simpler approach: replace the constant with jooqUdfHelper usage

    # First, let's find all lines with the pattern
    lines = content.split("\n")
    fixed_lines = []

    for line in lines:
        if "SqliteUdfDataSource.COLLATION_UNICODE_3" in line:
            # This is a complex replacement because we need to wrap the whole expression
            # The pattern is usually: field.collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
            # We need to change it to: jooqUdfHelper.run { field.collateUnicode3() }

            # Simple regex to capture field.collate(SqliteUdfDataSource.COLLATION_UNICODE_3)
            # But we need to handle nested parentheses
            line = re.sub(
                r"(\w+(?:\.\w+)*)\.collate\(SqliteUdfDataSource\.COLLATION_UNICODE_3\)",
                r"jooqUdfHelper.run { \1.collateUnicode3() }",
                line,
            )
        fixed_lines.append(line)

    content = "\n".join(fixed_lines)

    # Write back
    with open(file_path, "w") as f:
        f.write(content)

    print(f"Fixed {file_path}")


if __name__ == "__main__":
    fix_series_search_helper()
