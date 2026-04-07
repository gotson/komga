#!/usr/bin/env python3
import re


def fix_book_search_helper():
    file_path = "/Users/duong/Documents/GitHub/komga/komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/BookSearchHelper.kt"

    with open(file_path, "r") as f:
        content = f.read()

    # Fix collate(SqliteUdfDataSource.COLLATION_UNICODE_3) calls
    lines = content.split("\n")
    fixed_lines = []

    for line in lines:
        if "SqliteUdfDataSource.COLLATION_UNICODE_3" in line:
            # Replace field.collate(SqliteUdfDataSource.COLLATION_UNICODE_3) with jooqUdfHelper.run { field.collateUnicode3() }
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
    fix_book_search_helper()
