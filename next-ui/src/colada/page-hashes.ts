import { defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import type { PageHashAction } from '@/types/PageHashAction'

export const QUERY_KEYS_PAGE_HASHES = {
  unknown: ['page-hashes-unknown'] as const,
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
    key: ['page-hashes-known', { actions: actions, page: page, size: size, sort: sort }],
    query: () =>
      komgaClient
        .GET('/api/v1/page-hashes', {
          params: {
            query: {
              page: page,
              size: size,
              sort: sort,
              action: actions as PageHashAction[],
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)

export const pageHashesUnknownQuery = defineQueryOptions(
  ({ page, size, sort }: { page?: number; size?: number; sort?: string[] }) => ({
    key: [QUERY_KEYS_PAGE_HASHES.unknown, { page: page, size: size, sort: sort }],
    query: () =>
      komgaClient
        .GET('/api/v1/page-hashes/unknown', {
          params: {
            query: {
              page: page,
              size: size,
              sort: sort,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
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
    key: ['page-hash-matches', pageHash, { page: page, size: size, sort: sort }],
    query: () =>
      komgaClient
        .GET('/api/v1/page-hashes/{pageHash}', {
          params: {
            path: {
              pageHash: pageHash,
            },
            query: {
              page: page,
              size: size,
              sort: sort,
            },
          },
        })
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
    placeholderData: (previousData: any) => previousData, // eslint-disable-line @typescript-eslint/no-explicit-any
  }),
)
