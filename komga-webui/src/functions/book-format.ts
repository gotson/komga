export function getBookFormatFromMediaType (mediaType: string): BookFormat {
  switch (mediaType) {
    case 'application/x-rar-compressed':
      return { type: 'CBR', color: '#03A9F4' }
    case 'application/zip':
      return { type: 'CBZ', color: '#4CAF50' }
    case 'application/pdf':
      return { type: 'PDF', color: '#FF5722' }
    default:
      return { type: '?', color: '#000000' }
  }
}
