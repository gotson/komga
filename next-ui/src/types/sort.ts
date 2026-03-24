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
    message: defineMessage({
      description: 'Sort label: createdDate',
      defaultMessage: 'Date added',
      id: 'TG7prC',
    }),
    key: 'createdDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: lastModifiedDate',
      defaultMessage: 'Date updated',
      id: 'VHe28r',
    }),
    key: 'lastModifiedDate',
    initialOrder: 'desc',
    invertible: true,
  },
  {
    message: defineMessage({
      description: 'Sort label: readDate',
      defaultMessage: 'Date read',
      id: 'NasBHg',
    }),
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
