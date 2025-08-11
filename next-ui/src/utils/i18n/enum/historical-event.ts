import { defineMessage, type MessageDescriptor } from 'vue-intl'

// the keys are the event names for easier lookup
export const historicalEventMessages: Record<string, MessageDescriptor> = {
  BookFileDeleted: defineMessage({
    description: 'Historical event: BookFileDeleted',
    defaultMessage: 'Book File Deleted',
    id: 'enum.historicalEvent.BookFileDeleted',
  }),
  SeriesFolderDeleted: defineMessage({
    description: 'Historical event: SeriesFolderDeleted',
    defaultMessage: 'Series Folder Deleted',
    id: 'enum.historicalEvent.SeriesFolderDeleted',
  }),
  DuplicatePageDeleted: defineMessage({
    description: 'Historical event: DuplicatePageDeleted',
    defaultMessage: 'Duplicate Page Deleted',
    id: 'enum.historicalEvent.DuplicatePageDeleted',
  }),
  BookConverted: defineMessage({
    description: 'Historical event: ',
    defaultMessage: 'Book Converted',
    id: 'enum.historicalEvent.BookConverted',
  }),
  BookImported: defineMessage({
    description: 'Historical event: BookImported',
    defaultMessage: 'Book Imported',
    id: 'enum.historicalEvent.BookImported',
  }),
}
