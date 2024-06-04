export interface TocEntry {
  title: string,
  href?: string,
  children?: TocEntry[],
  current?: boolean,
  level?: number,
}
