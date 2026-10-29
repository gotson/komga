export type InitialCountItem<T> = {
  value: T
  count: number
}

export type BulkInputProps<T> = {
  initialCounts: InitialCountItem<T>[]
  totalCount: number
}

export type BulkItemSingleField<T> =
  | {
      state: 'mixed'
    }
  | {
      value: T
      state: 'all'
    }

export type BulkItemMultiField<T> =
  | {
      value: T
      state: 'mixed'
      count: number
    }
  | {
      value: T
      state: 'all' | 'new' | 'remove'
    }
