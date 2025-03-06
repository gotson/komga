import {BookFormat, MediaDto} from '@/types/komga-books'
import {lowerCase} from 'lodash'

export function getBookFormatFromMedia(media: MediaDto): BookFormat {
  switch (media.mediaType) {
    case 'application/x-rar-compressed':
    case 'application/x-rar-compressed; version=4':
      return {type: 'CBR', color: '#03A9F4'}
    case 'application/zip':
      return {type: 'CBZ', color: '#4CAF50'}
    case 'application/pdf':
      return {type: 'PDF', color: '#FF5722'}
    case 'application/epub+zip':
      return media.epubIsKepub ? {type: 'KEPUB', color: '#ff5ab1'} : {type: 'EPUB', color: '#ff5ab1'}
    case 'application/x-rar-compressed; version=5':
      return {type: 'RAR5', color: '#000000'}
    default:
      return {type: media.mediaType, color: '#000000'}
  }
}

export function getBookFormatFromMediaType(mediaType: string): BookFormat {
  switch (mediaType) {
    case 'application/x-rar-compressed':
    case 'application/x-rar-compressed; version=4':
      return {type: 'CBR', color: '#03A9F4'}
    case 'application/zip':
      return {type: 'CBZ', color: '#4CAF50'}
    case 'application/pdf':
      return {type: 'PDF', color: '#FF5722'}
    case 'application/epub+zip':
      return {type: 'EPUB', color: '#ff5ab1'}
    case 'application/x-rar-compressed; version=5':
      return {type: 'RAR5', color: '#000000'}
    default:
      return {type: mediaType, color: '#000000'}
  }
}

export function getBookReadRouteFromMedia(media: MediaDto): string {
  switch (lowerCase(media.mediaProfile)) {
    case 'epub':
      return media.epubDivinaCompatible ? 'read-book' : 'read-epub'
    default:
      return 'read-book'
  }
}
