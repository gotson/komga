export enum ContextOrigin {
  SERIES = 'SERIES',
  READLIST = 'READLIST',
  LIBRARY = 'LIBRARY',
  COLLECTION = 'COLLECTION'
}

export interface Context {
  origin: ContextOrigin,
  id: string,
}
