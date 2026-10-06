import { ApiBaseUrl } from '@/api/base'

export function bookReaderUrl(
  bookId: string | undefined,
  epub: boolean,
  incognito: boolean = false,
  readListId?: string,
): string | undefined {
  if (bookId) {
    let url = `${ApiBaseUrl.noSlash}/classic/book/${bookId}/read${epub ? '-epub' : ''}?incognito=${incognito}`
    if (readListId) url += `&context=READLIST&contextId=${readListId}`
    return url
  }
  return undefined
}

export function oauth2LoginUrl(registrationId: string | undefined): string | undefined {
  if (registrationId) return `${ApiBaseUrl.noSlash}/oauth2/authorization/${registrationId}`
}
