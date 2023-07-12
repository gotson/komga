export interface JsonFeedDto {
  version: string,
  title: string,
  home_page_url?: string,
  description?: string,
  items: ItemDto[],
}

export interface ItemDto {
  id: string,
  url?: string,
  title?: string,
  summary?: string,
  content_html?: string,
  date_modified?: Date,
  author: AuthorDto,
  tags: Set<string>,
  _komga?: KomgaExtensionDto,
}

export interface AuthorDto {
  name?: string,
  url?: string,
}

export interface KomgaExtensionDto {
  read: boolean,
}
