export interface HistoricalEventDto {
  type: string,
  timestamp: string,
  bookId?: string,
  seriesId?: string,
  properties: Record<string, string>[],
}
