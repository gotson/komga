export interface HistoricalEventDto {
  type: string,
  timestamp: Date,
  bookId?: string,
  seriesId?: string,
  properties: Record<string, string>[],
}
