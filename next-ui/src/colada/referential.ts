import { defineInfiniteQueryOptions, defineQueryOptions } from '@pinia/colada'
import { PageRequest } from '@/types/PageRequest'
import {
  komgaGetAgeRatings,
  komgaGetAuthors,
  komgaGetAuthorsNames,
  komgaGetAuthorsRoles,
  komgaGetGenres,
  komgaGetLanguages,
  komgaGetPublishers,
  komgaGetSeriesReleaseYears,
  komgaGetSharingLabels,
  komgaGetTags,
} from '@/generated/openapi'

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
        komgaGetAuthors({
          query: queryParams,
        }),
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
        komgaGetAuthorsRoles({
          query: queryParams,
        }),
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
        komgaGetAuthorsNames({
          query: { ...queryParams, ...pageParam },
        }),
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
        komgaGetGenres({
          query: queryParams,
        }),
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
        komgaGetGenres({
          query: { ...queryParams, ...pageParam },
        }),
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
        komgaGetTags({
          query: queryParams,
        }),
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
        komgaGetTags({
          query: { ...queryParams, ...pageParam },
        }),
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
        komgaGetPublishers({
          query: queryParams,
        }),
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
        komgaGetPublishers({
          query: {
            ...queryParams,
            ...pageParam,
          },
        }),
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
        komgaGetSharingLabels({
          query: queryParams,
        }),
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
        komgaGetSharingLabels({
          query: { ...queryParams, ...pageParam },
        }),
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
        komgaGetLanguages({
          query: queryParams,
        }),
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
        komgaGetSeriesReleaseYears({
          query: queryParams,
        }),
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
        komgaGetAgeRatings({
          query: queryParams,
        }),
    }
  },
)
