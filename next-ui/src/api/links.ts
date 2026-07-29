import { ApiBaseUrl } from '@/api/base'

export function bookReaderUrl(
  bookId: string | undefined,
  epub: boolean,
  incognito: boolean = false,
  readListId?: string,
): string | undefined {
  if (bookId) {
    let url = `${ApiBaseUrl.noSlash}/book/${bookId}/read${epub ? '-epub' : ''}?incognito=${incognito}`
    if (readListId) url += `&context=READLIST&contextId=${readListId}`
    return url
  }
  return undefined
}
