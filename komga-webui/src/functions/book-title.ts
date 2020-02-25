export function getBookTitleCompact (bookTitle: string, seriesTitle: string): string {
  if (bookTitle?.toLowerCase().includes(seriesTitle?.toLowerCase())) {
    return bookTitle
  } else {
    return `${seriesTitle} - ${bookTitle}`
  }
}
