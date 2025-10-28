import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

export const historyBookImported = {
  id: 'H1',
  type: 'BookImported',
  timestamp: new Date('2025-08-07T09:36:22.596'),
  bookId: 'B5',
  seriesId: 'S1',
  properties: {
    name: '/data/Books/Donjon Parade/Donjon Parade - 09 - Nécomancien pour de faux.cbz',
    source: '/data/Import/Donjon Parade - 9 - Nécomancien pour de faux (Sfar-Trandheim-Delaf).cbz',
    upgrade: 'No',
  },
}

export const historySeriesFolderDeleted = {
  id: 'H2',
  type: 'SeriesFolderDeleted',
  timestamp: new Date('2025-08-05T21:33:56.452'),
  seriesId: '404',
  properties: {
    name: '/data/Books/One Piece',
    reason: 'Folder was deleted because it was empty',
  },
}

export const historyBookFileDeleted = {
  id: 'H3',
  type: 'BookFileDeleted',
  timestamp: new Date('2025-08-05T21:33:56.445'),
  bookId: '404',
  seriesId: 'S2',
  properties: {
    name: '/data/Books/One Piece/Volume 1.cbz',
    reason: 'File was deleted by user request',
  },
}

export const historyBookImportedForUpgrade = {
  id: 'H4',
  type: 'BookImported',
  timestamp: new Date('2025-07-28T17:52:14.126'),
  bookId: 'B15',
  seriesId: 'S3',
  properties: {
    name: '/data/Lastman/Lastman - T12.cbz',
    source: '/data/Import/Lastman/Lastman_-_Nouvelle_Édition_T12.cbz',
    upgrade: 'Yes',
  },
}

export const historyBookFileDeletedForUpgrade = {
  id: 'H5',
  type: 'BookFileDeleted',
  timestamp: new Date('2025-07-28T17:52:14.016'),
  bookId: 'b14',
  seriesId: 'S3',
  properties: {
    name: '/data/Lastman/Lastman - 12.cbz',
    reason: 'File was deleted to import an upgrade',
  },
}

export const historyDuplicatePageDeleted = {
  id: 'H6',
  type: 'DuplicatePageDeleted',
  timestamp: new Date('2025-07-26T21:33:18.809'),
  bookId: 'B5',
  seriesId: 'S2',
  properties: {
    name: '/data/Lastman/Volume 6.cbz',
    'page file hash': 'e3f8f3814609645d6ffc6f6f2c4c65aa',
    'page file name': 'credits.jpg',
    'page file size': '1204289',
    'page media type': 'image/jpeg',
    'page number': '34',
  },
}

export const historyBookConverted = {
  id: 'H7',
  type: 'BookConverted',
  timestamp: new Date('2025-07-07T17:33:39.545'),
  bookId: 'B25',
  seriesId: 'S21',
  properties: {
    'former file': '/data/Usagi Yojimbo/Usagi Yojimbo (Book 40) - The Crow (2025).cbr',
    name: '/data/Usagi Yojimbo/Usagi Yojimbo (Book 40) - The Crow (2025).cbz',
  },
}

const history = [
  historyBookImported,
  historySeriesFolderDeleted,
  historyBookFileDeleted,
  historyBookFileDeletedForUpgrade,
  historyBookImportedForUpgrade,
  historyDuplicatePageDeleted,
  historyBookConverted,
]

export const historyHandlers = [
  httpTyped.get('/api/v1/history', ({ query, response }) =>
    response(200).json(
      mockPage(
        history,
        new PageRequest(Number(query.get('page')), Number(query.get('size')), query.getAll('sort')),
      ),
    ),
  ),
]
