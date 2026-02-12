import { httpTyped } from '@/mocks/api/httpTyped'
import type { components } from '@/generated/openapi/komga'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

const sharingLabels = ['kids', 'teens']

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

export const referentialHandlers = [
  httpTyped.get('/api/v1/sharing-labels', ({ response }) => response(200).json(sharingLabels)),
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
]
