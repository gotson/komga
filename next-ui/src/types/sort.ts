import { defineMessage, type MessageDescriptor } from 'vue-intl'

export type SortOption = {
  // for display
  label: string
  // sorting key sent to API
  key: string
  // initial order
  initialOrder: SortOrder
  // whether the order can be flipped
  invertible: boolean
}

export type SortOrder = 'asc' | 'desc'

export type SortOptionDescriptor = Omit<SortOption, 'label'> & { message: MessageDescriptor }

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

export const sortSeries: SortOptionDescriptor[] = [
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
]

export const sortBooks: SortOptionDescriptor[] = [
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
    key: 'readDate',
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
    initialOrder: 'asc',
    invertible: false,
  },
]
