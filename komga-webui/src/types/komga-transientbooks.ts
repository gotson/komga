import {PageDto} from '@/types/komga-books'

export interface TransientBookDto {
  id: string,
  name: string,
  url: string,
  fileLastModified: string,
  sizeBytes: number,
  size: string,
  status: string,
  mediaType: string,
  pages: PageDto[],
  files: string[],
  comment: string,
}
