#!/usr/bin/env python3
import re


def fix_referential_dao():
    file_path = "/Users/duong/Documents/GitHub/komga/komga/src/main/kotlin/org/gotson/komga/infrastructure/jooq/main/ReferentialDao.kt"

    with open(file_path, "r") as f:
        content = f.read()

    # Fix udfStripAccents() calls
    # Pattern: field.udfStripAccents() -> jooqUdfHelper.run { field.udfStripAccents() }
    # But need to be careful not to match already fixed ones
    pattern1 = r"(\w+\.\w+\.udfStripAccents\(\))"

    def replace_udf(match):
        field_expr = match.group(1)
        return f"jooqUdfHelper.run {{ {field_expr} }}"

    # First pass: fix udfStripAccents
    content = re.sub(pattern1, replace_udf, content)

    # Fix collate(SqliteUdfDataSource.COLLATION_UNICODE_3) calls
    # Pattern: field.collate(SqliteUdfDataSource.COLLATION_UNICODE_3) -> jooqUdfHelper.run { field.collateUnicode3() }
    pattern2 = r"(\w+\.\w+)\.collate\(SqliteUdfDataSource\.COLLATION_UNICODE_3\)"

    def replace_collate(match):
        field_expr = match.group(1)
        return f"jooqUdfHelper.run {{ {field_expr}.collateUnicode3() }}"

    # Second pass: fix collate
    content = re.sub(pattern2, replace_collate, content)

    # Write back
    with open(file_path, "w") as f:
        f.write(content)

    print(f"Fixed {file_path}")


if __name__ == "__main__":
    fix_referential_dao()
