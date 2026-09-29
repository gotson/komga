import { defineMessage, type MessageDescriptor } from 'vue-intl'

export type SortDefinition = {
  // sorting key sent to API
  key: string
  // initial order
  initialOrder: SortOrder
  // whether the order can be flipped
  invertible: boolean
}

export type SortOption = {
  // for display
  label: string
} & SortDefinition

export type SortOrder = 'asc' | 'desc'

export type SortOptionDescriptor = Omit<SortOption, 'label'> & { message: MessageDescriptor }

type ResolveOrder<T extends readonly SortDefinition[]> = {
  [K in keyof T]: T[K] extends {
    key: infer KKey extends string
    initialOrder: infer KOrder
    invertible: infer KInvert
  }
    ? KInvert extends true
      ? { key: KKey; order: SortOrder }
      : { key: KKey; order: KOrder }
    : never
}[number]

const messages = {
  createdDate: defineMessage({
    description: 'Sort label: createdDate',
    defaultMessage: 'Date added',
    id: 'TG7prC',
  }),
  lastModifiedDate: defineMessage({
    description: 'Sort label: lastModifiedDate',
    defaultMessage: 'Date updated',
    id: 'VHe28r',
  }),
  readDate: defineMessage({
    description: 'Sort label: readDate',
    defaultMessage: 'Date read',
    id: 'NasBHg',
  }),
}

export const sortSeries = [
  {
    message: defineMessage({
      description: 'Sort label: metadata.titleSort',
      defaultMessage: 'Title',
      id: 'H4Kte4',
    }),
    key: 'metadata.titleSort',
    initialOrder: 'asc',
    invertible: true,
  },
  {
    message: messages.createdDate,
    key: 'createdDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: messages.lastModifiedDate,
    key: 'lastModifiedDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: messages.readDate,
    key: 'readDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: booksMetadata.releaseDate',
      defaultMessage: 'Release year',
      id: 'J8rAqm',
    }),
    key: 'booksMetadata.releaseDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: name',
      defaultMessage: 'Directory name',
      id: 'DNVnmS',
    }),
    key: 'name',
    initialOrder: 'asc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: booksCount',
      defaultMessage: 'Books count',
      id: 'TAVSfO',
    }),
    key: 'booksCount',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: random',
      defaultMessage: 'Random',
      id: 'Vwpr+D',
    }),
    key: 'random',
    initialOrder: 'asc',
    invertible: false,
  },
] as const satisfies readonly SortOptionDescriptor[]

export type SortKeysSeries = (typeof sortSeries)[number]['key']
export type SortSeries = ResolveOrder<typeof sortSeries>

export const sortBooks = [
  {
    message: defineMessage({
      description: 'Sort label: series',
      defaultMessage: 'Series',
      id: 'X47Js+',
    }),
    key: 'series',
    initialOrder: 'asc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: metadata.numberSort',
      defaultMessage: 'Number',
      id: 'r/G7j0',
    }),
    key: 'metadata.numberSort',
    initialOrder: 'asc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: metadata.title',
      defaultMessage: 'Name',
      id: 'nXWSTf',
    }),
    key: 'metadata.title',
    initialOrder: 'asc',
    invertible: true,
  },
  {
    message: messages.createdDate,
    key: 'createdDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: messages.lastModifiedDate,
    key: 'lastModifiedDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: metadata.releaseDate',
      defaultMessage: 'Release date',
      id: 'Uj479p',
    }),
    key: 'metadata.releaseDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: messages.readDate,
    key: 'readProgress.readDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: fileSize',
      defaultMessage: 'File size',
      id: 'Y/fJj5',
    }),
    key: 'fileSize',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: name',
      defaultMessage: 'File name',
      id: '+jaADC',
    }),
    key: 'name',
    initialOrder: 'asc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: pagesCount',
      defaultMessage: 'Page count',
      id: 'WVblsI',
    }),
    key: 'pagesCount',
    initialOrder: 'desc',
    invertible: true,
  },
] as const satisfies readonly SortOptionDescriptor[]

export type SortKeysBook = (typeof sortBooks)[number]['key']
export type SortBook = ResolveOrder<typeof sortBooks>
