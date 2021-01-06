export enum ContextOrigin {
  SERIES = 'SERIES',
  READLIST = 'READLIST'
}

export interface Context {
  origin: ContextOrigin,
  id: string,
}
