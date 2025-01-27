export function getBookTitleCompact(bookTitle: string, seriesTitle: string, number?: string): string {
  if (bookTitle?.toLowerCase().includes(seriesTitle?.toLowerCase())) {
    return bookTitle
  } else {
    if (number != undefined)
      return `${seriesTitle} - ${number} - ${bookTitle}`
    else
      return `${seriesTitle} - ${bookTitle}`
  }
}
