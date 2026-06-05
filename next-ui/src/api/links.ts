import { ApiBaseUrl } from '@/api/base'

export function bookReaderUrl(
  bookId: string | undefined,
  epub: boolean,
  incognito: boolean = false,
): string | undefined {
  if (bookId)
    return `${ApiBaseUrl.noSlash}/book/${bookId}/read${epub ? '-epub' : ''}?incognito=${incognito}`
  return undefined
}
