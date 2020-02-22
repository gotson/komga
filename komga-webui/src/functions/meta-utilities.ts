import KomgaSeriesService from '@/services/komga-series.service'

export async function getBookTitle (komgaSeries: KomgaSeriesService, book: BookDto): Promise<string> {
  return komgaSeries.getOneSeries(book.seriesId).then(series => {
    if (book.name.toLowerCase().includes(series.name.toLowerCase())) {
      return book.name
    } else {
      return `${series.name} - ${book.name}`
    }
  })
}
