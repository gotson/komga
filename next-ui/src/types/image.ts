export type ImageDimensions = { width: number; height: number }

export const MEDIA_TYPE_COLORS: Record<string, string> = {
  'image/jpeg': 'blue',
  'image/png': 'deep-purple',
  'image/webp': 'teal',
  'image/avif': 'pink',
  'image/jxl': 'purple',
  'image/gif': 'orange',
  'image/svg+xml': 'red',
  'image/heic': 'cyan',
  'image/bmp': 'grey',
  'image/tiff': 'indigo',
  'image/ico': 'blue-grey',
}

// Fallback color for unlisted media types
const DEFAULT_COLOR: string = 'grey'

export function getMediaTypeColor(mediaType: string): string {
  return MEDIA_TYPE_COLORS[mediaType] ?? DEFAULT_COLOR
}
