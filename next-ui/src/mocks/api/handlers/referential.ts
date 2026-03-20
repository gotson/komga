import { httpTyped } from '@/mocks/api/httpTyped'
import type { components } from '@/generated/openapi/komga'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

const authorRoles = [
  'writer',
  'penciller',
  'inker',
  'colorist',
  'letterer',
  'cover',
  'editor',
  'translator',
]

function doMockAuthors(count: number) {
  return [...Array(count).keys()].map((index) => {
    const role = authorRoles[index % authorRoles.length]
    return {
      role: role,
      name: `Author ${index} (${role})`,
    } as components['schemas']['AuthorDto']
  })
}
const mockAuthors = doMockAuthors(10000)

function doMockStrings(count: number, prefix: string) {
  return [...Array(count).keys()].map((index) => {
    return `${prefix} ${index}`
  })
}
const mockGenres = doMockStrings(10000, 'Genre')
const mockTags = doMockStrings(10000, 'Tag')
const mockPublishers = doMockStrings(10000, 'Publisher')
const mockSharingLabels = doMockStrings(150, 'SharingLabel')
const mockLanguages = ['de', 'en', 'en-US', 'es', 'fr', 'fr-CA', 'ja', 'it']
const mockReleaseYears = ['2022', '2021', '2020', '2019', '2018', '2016', '1988', '1970']

function filterAndPage(
  search: string | null,
  data: string[],
  page: string | null,
  size: string | null,
  unpaged: string | null,
) {
  const selected = search ? data.filter((it) => !!it.match(new RegExp(search, 'i'))) : data

  return mockPage(
    selected,
    new PageRequest(Number(page), Number(size), undefined, Boolean(unpaged)),
  )
}

export const referentialHandlers = [
  httpTyped.get('/api/v2/genres', ({ query, response }) =>
    response(200).json(
      filterAndPage(
        query.get('search'),
        mockGenres,
        query.get('page'),
        query.get('size'),
        query.get('unpaged'),
      ),
    ),
  ),
  httpTyped.get('/api/v2/tags', ({ query, response }) =>
    response(200).json(
      filterAndPage(
        query.get('search'),
        mockTags,
        query.get('page'),
        query.get('size'),
        query.get('unpaged'),
      ),
    ),
  ),
  httpTyped.get('/api/v2/publishers', ({ query, response }) =>
    response(200).json(
      filterAndPage(
        query.get('search'),
        mockPublishers,
        query.get('page'),
        query.get('size'),
        query.get('unpaged'),
      ),
    ),
  ),
  httpTyped.get('/api/v2/sharing-labels', ({ query, response }) =>
    response(200).json(
      filterAndPage(
        query.get('search'),
        mockSharingLabels,
        query.get('page'),
        query.get('size'),
        query.get('unpaged'),
      ),
    ),
  ),
  httpTyped.get('/api/v2/languages', ({ query, response }) =>
    response(200).json(
      filterAndPage(
        query.get('search'),
        mockLanguages,
        query.get('page'),
        query.get('size'),
        query.get('unpaged'),
      ),
    ),
  ),
  httpTyped.get('/api/v2/series/release-years', ({ query, response }) =>
    response(200).json(
      filterAndPage(
        null,
        mockReleaseYears,
        query.get('page'),
        query.get('size'),
        query.get('unpaged'),
      ),
    ),
  ),
  httpTyped.get('/api/v2/authors', ({ query, response }) => {
    const search = query.get('search')
    const role = query.get('role')
    const selected = search
      ? mockAuthors.filter((it) => !!it.name.match(new RegExp(search, 'i')))
      : mockAuthors
    const byRole = role ? selected.filter((it) => it.role === role) : selected

    return response(200).json(
      mockPage(
        byRole,
        new PageRequest(
          Number(query.get('page')),
          Number(query.get('size')),
          undefined,
          Boolean(query.get('unpaged')),
        ),
      ),
    )
  }),
  httpTyped.get('/api/v2/authors/names', ({ query, response }) => {
    const search = query.get('search')
    const role = query.get('role')
    const selected = search
      ? mockAuthors.filter((it) => !!it.name.match(new RegExp(search, 'i')))
      : mockAuthors
    const byRole = role ? selected.filter((it) => it.role === role) : selected
    const names = [...new Set(byRole.map((it) => it.name))]

    return response(200).json(
      mockPage(
        names,
        new PageRequest(
          Number(query.get('page')),
          Number(query.get('size')),
          undefined,
          Boolean(query.get('unpaged')),
        ),
      ),
    )
  }),
  httpTyped.get('/api/v2/authors/roles', ({ query, response }) => {
    const roles = [...new Set(mockAuthors.map((it) => it.role))]

    return response(200).json(
      mockPage(
        roles,
        new PageRequest(
          Number(query.get('page')),
          Number(query.get('size')),
          undefined,
          Boolean(query.get('unpaged')),
        ),
      ),
    )
  }),
]
