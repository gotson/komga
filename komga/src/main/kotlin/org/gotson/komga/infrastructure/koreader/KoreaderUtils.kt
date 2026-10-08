package org.gotson.komga.infrastructure.koreader

object KoreaderUtils {
  private val xpathBracketsRegex = Regex("""DocFragment\[(\d+)]""", RegexOption.IGNORE_CASE)
  private val xpathNoBracketsRegex = Regex("""DocFragment/""", RegexOption.IGNORE_CASE)
  private val anchorRegex = Regex("""#_doc_fragment_(\d+)_""", RegexOption.IGNORE_CASE)

  /**
   * Try to parse the resource index from a Koreader progress string.
   * @return the 0-based resource index
   */
  fun parseResourceIndexFromProgressString(progress: String): Int? {
    xpathBracketsRegex
      .find(progress)
      ?.groups
      // capturing group is at index 1, 0 is the full match
      ?.get(1)
      ?.value
      ?.toIntOrNull()
      // KOReader indexing for this format starts at 1
      ?.minus(1)
      ?.let { return it }

    if (xpathNoBracketsRegex.containsMatchIn(progress)) return 0

    anchorRegex
      .find(progress)
      ?.groups
      // capturing group is at index 1, 0 is the full match
      ?.get(1)
      ?.value
      // KOReader indexing for this format starts at 0
      ?.toIntOrNull()
      ?.let { return it }

    return null
  }
}
