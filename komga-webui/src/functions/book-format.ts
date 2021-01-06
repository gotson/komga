import { BookFormat } from '@/types/komga-books'

export function getBookFormatFromMediaType (mediaType: string): BookFormat {
  switch (mediaType) {
    case 'application/x-rar-compressed':
    case 'application/x-rar-compressed; version=4':
      return { type: 'CBR', color: '#03A9F4' }
    case 'application/zip':
      return { type: 'CBZ', color: '#4CAF50' }
    case 'application/pdf':
      return { type: 'PDF', color: '#FF5722' }
    case 'application/epub+zip':
      return { type: 'EPUB', color: '#ff5ab1' }
    default:
      return { type: '?', color: '#000000' }
  }
}
