import { defineQueryOptions } from '@pinia/colada'
import type { PageHashAction } from '@/types/PageHashAction'
import {
  komgaGetKnownPageHashes,
  komgaGetPageHashMatches,
  komgaGetUnknownPageHashes,
} from '@/generated/openapi'

export const QUERY_KEYS_PAGE_HASHES = {
  root: ['page-hashes'] as const,
  known: () => [...QUERY_KEYS_PAGE_HASHES.root, 'known'] as const,
  unknown: () => [...QUERY_KEYS_PAGE_HASHES.root, 'unknown'] as const,
}

export const pageHashesKnownQuery = defineQueryOptions(
  ({
    actions,
    page,
    size,
    sort,
  }: {
    actions?: string[]
    page?: number
    size?: number
    sort?: string[]
  }) => ({
    key: [
      ...QUERY_KEYS_PAGE_HASHES.known(),
      { actions: actions, page: page, size: size, sort: sort },
    ],
    query: () =>
      komgaGetKnownPageHashes({
        query: {
          page: page,
          size: size,
          sort: sort,
          action: actions as PageHashAction[],
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const pageHashesUnknownQuery = defineQueryOptions(
  ({ page, size, sort }: { page?: number; size?: number; sort?: string[] }) => ({
    key: [...QUERY_KEYS_PAGE_HASHES.unknown(), { page: page, size: size, sort: sort }],
    query: () =>
      komgaGetUnknownPageHashes({
        query: {
          page: page,
          size: size,
          sort: sort,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)

export const pageHashMatchesQuery = defineQueryOptions(
  ({
    pageHash,
    page,
    size,
    sort,
  }: {
    pageHash: string
    page?: number
    size?: number
    sort?: string[]
  }) => ({
    key: [...QUERY_KEYS_PAGE_HASHES.known(), pageHash, { page: page, size: size, sort: sort }],
    query: () =>
      komgaGetPageHashMatches({
        path: {
          pageHash: pageHash,
        },
        query: {
          page: page,
          size: size,
          sort: sort,
        },
      }),
    placeholderData: (previousData) => previousData,
  }),
)
