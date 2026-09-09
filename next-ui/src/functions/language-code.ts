const displayNames = new Intl.DisplayNames(['en'], {
  type: 'language',
  fallback: 'none', // Returns undefined if invalid subtags are present
})

export function isValidLanguageCode(tag: string): boolean {
  try {
    // 1. Check syntax via Intl.Locale
    const loc = new Intl.Locale(tag.trim())

    // 2. Query engine's CLDR data for real language existence
    return displayNames.of(loc.toString()) !== undefined
  } catch {
    return false // Threw RangeError due to invalid syntax
  }
}
