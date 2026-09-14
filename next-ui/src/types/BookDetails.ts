import type { BookDto, PageDto, TransientBookDto } from '@/generated/openapi'
import { bookPageUrl, transientBookPageUrl } from '@/api/images'

export type BookDetails = {
  filename: string
  fileSize: number
  mediaType: string
  pages: PageDtoWithUrl[]
}

export function transientBookDtoToBookDetails(transientBook: TransientBookDto): BookDetails {
  return {
    filename: transientBook.url,
    fileSize: transientBook.sizeBytes,
    mediaType: transientBook.mediaType,
    pages: transientBook.pages.map((it) => ({
      ...it,
      url: transientBookPageUrl(transientBook.id, it.number),
    })),
  }
}

export function bookDtoToBookDetails(bookDto: BookDto, pages: PageDto[]): BookDetails {
  return {
    filename: bookDto.url,
    fileSize: bookDto.sizeBytes,
    mediaType: bookDto.media.mediaType,
    pages: pages.map((it) => ({
      ...it,
      url: bookPageUrl(bookDto.id, it.number),
    })),
  }
}

export type PageDtoWithUrl = PageDto & { url?: string }
