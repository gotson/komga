import { defineInfiniteQueryOptions, defineQueryOptions } from '@pinia/colada'
import { komgaClient } from '@/api/komga-client'
import { PageRequest } from '@/types/PageRequest'

export const authorsQuery = defineQueryOptions(
  ({
    search,
    role,
    library_id,
    collection_id,
    series_id,
    readlist_id,
    pageRequest,
  }: {
    search?: string
    role?: string
    library_id?: string[]
    collection_id?: string[]
    series_id?: string[]
    readlist_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      search: search,
      role: role,
      library_id: library_id,
      collection_id: collection_id,
      series_id: series_id,
      readlist_id: readlist_id,
      ...pageRequest,
    }
    return {
      key: ['authors', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/authors', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const authorsRolesQuery = defineQueryOptions(
  ({
    library_id,
    collection_id,
    series_id,
    readlist_id,
    pageRequest,
  }: {
    library_id?: string[]
    collection_id?: string[]
    series_id?: string[]
    readlist_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
      series_id: series_id,
      readlist_id: readlist_id,
      ...pageRequest,
    }
    return {
      key: ['authors', 'roles', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/authors/roles', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const authorsNamesQueryInfinite = defineInfiniteQueryOptions(
  ({
    role,
    library_id,
    collection_id,
    series_id,
    readlist_id,
  }: {
    role?: string
    library_id?: string[]
    collection_id?: string[]
    series_id?: string[]
    readlist_id?: string[]
  }) => {
    const queryParams = {
      role: role,
      library_id: library_id,
      collection_id: collection_id,
      series_id: series_id,
      readlist_id: readlist_id,
    }
    return {
      key: ['authors', 'names', queryParams, { infinite: true }],
      initialPageParam: new PageRequest(0, 50),
      query: ({ pageParam }) =>
        komgaClient
          .GET('/api/v2/authors/names', {
            params: {
              query: { ...queryParams, ...pageParam },
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
      getNextPageParam: (lastPage, _, lastPageParam) =>
        !lastPage?.last ? lastPageParam.next() : null,
    }
  },
)

export const genresQuery = defineQueryOptions(
  ({
    search,
    library_id,
    collection_id,
    pageRequest,
  }: {
    search?: string
    library_id?: string[]
    collection_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      search: search,
      library_id: library_id,
      collection_id: collection_id,
      ...pageRequest,
    }
    return {
      key: ['genres', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/genres', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const genresQueryInfinite = defineInfiniteQueryOptions(
  ({ library_id, collection_id }: { library_id?: string[]; collection_id?: string[] }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
    }
    return {
      key: ['genres', queryParams, { infinite: true }],
      initialPageParam: new PageRequest(0, 50),
      query: ({ pageParam }) =>
        komgaClient
          .GET('/api/v2/genres', {
            params: {
              query: { ...queryParams, ...pageParam },
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
      getNextPageParam: (lastPage, _, lastPageParam) =>
        !lastPage?.last ? lastPageParam.next() : null,
    }
  },
)

export const tagsQuery = defineQueryOptions(
  ({
    search,
    library_id,
    collection_id,
    series_id,
    readlist_id,
    include,
    pageRequest,
  }: {
    search?: string
    library_id?: string[]
    collection_id?: string[]
    series_id?: string[]
    readlist_id?: string[]
    include?: 'SERIES' | 'BOOK' | 'BOTH'
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      search: search,
      library_id: library_id,
      collection_id: collection_id,
      series_id: series_id,
      readlist_id: readlist_id,
      include: include,
      ...pageRequest,
    }
    return {
      key: ['tags', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/tags', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const tagsQueryInfinite = defineInfiniteQueryOptions(
  ({
    library_id,
    collection_id,
    series_id,
    readlist_id,
    include,
  }: {
    library_id?: string[]
    collection_id?: string[]
    series_id?: string[]
    readlist_id?: string[]
    include?: 'SERIES' | 'BOOK' | 'BOTH'
  }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
      series_id: series_id,
      readlist_id: readlist_id,
      include: include,
    }
    return {
      key: ['tags', queryParams, { infinite: true }],
      initialPageParam: new PageRequest(0, 50),
      query: ({ pageParam }) =>
        komgaClient
          .GET('/api/v2/tags', {
            params: {
              query: { ...queryParams, ...pageParam },
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
      getNextPageParam: (lastPage, _, lastPageParam) =>
        !lastPage?.last ? lastPageParam.next() : null,
    }
  },
)

export const publishersQuery = defineQueryOptions(
  ({
    search,
    library_id,
    collection_id,
    pageRequest,
  }: {
    search?: string
    library_id?: string[]
    collection_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      search: search,
      library_id: library_id,
      collection_id: collection_id,
      ...pageRequest,
    }
    return {
      key: ['publishers', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/publishers', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const publishersQueryInfinite = defineInfiniteQueryOptions(
  ({ library_id, collection_id }: { library_id?: string[]; collection_id?: string[] }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
    }
    return {
      key: ['publishers', queryParams, { infinite: true }],
      initialPageParam: new PageRequest(0, 50),
      query: ({ pageParam }) =>
        komgaClient
          .GET('/api/v2/publishers', {
            params: {
              query: {
                ...queryParams,
                ...pageParam,
              },
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
      getNextPageParam: (lastPage, _, lastPageParam) =>
        !lastPage?.last ? lastPageParam.next() : null,
    }
  },
)

export const sharingLabelsQuery = defineQueryOptions(
  ({
    search,
    library_id,
    collection_id,
    pageRequest,
  }: {
    search?: string
    library_id?: string[]
    collection_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      search: search,
      library_id: library_id,
      collection_id: collection_id,
      ...pageRequest,
    }
    return {
      key: ['sharing-labels', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/sharing-labels', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const sharingLabelsQueryInfinite = defineInfiniteQueryOptions(
  ({ library_id, collection_id }: { library_id?: string[]; collection_id?: string[] }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
    }
    return {
      key: ['sharing-labels', queryParams, { infinite: true }],
      initialPageParam: new PageRequest(0, 50),
      query: ({ pageParam }) =>
        komgaClient
          .GET('/api/v2/sharing-labels', {
            params: {
              query: { ...queryParams, ...pageParam },
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
      getNextPageParam: (lastPage, _, lastPageParam) =>
        !lastPage?.last ? lastPageParam.next() : null,
    }
  },
)

export const languagesQuery = defineQueryOptions(
  ({
    search,
    library_id,
    collection_id,
    pageRequest,
  }: {
    search?: string
    library_id?: string[]
    collection_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      search: search,
      library_id: library_id,
      collection_id: collection_id,
      ...pageRequest,
    }
    return {
      key: ['languages', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/languages', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const releaseYearsQuery = defineQueryOptions(
  ({
    library_id,
    collection_id,
    pageRequest,
  }: {
    library_id?: string[]
    collection_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
      ...pageRequest,
    }
    return {
      key: ['release-years', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/series/release-years', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)

export const ageRatingsQuery = defineQueryOptions(
  ({
    library_id,
    collection_id,
    pageRequest,
  }: {
    library_id?: string[]
    collection_id?: string[]
    pageRequest?: PageRequest
  }) => {
    const queryParams = {
      library_id: library_id,
      collection_id: collection_id,
      ...pageRequest,
    }
    return {
      key: ['age-ratings', queryParams],
      query: () =>
        komgaClient
          .GET('/api/v2/age-ratings', {
            params: {
              query: queryParams,
            },
          })
          // unwrap the openapi-fetch structure on success
          .then((res) => res.data),
    }
  },
)
export class authorsQueryInfinite {}
