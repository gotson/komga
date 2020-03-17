import { get, groupBy, mapKeys, mapValues } from 'lodash'
import { authorRoles } from '@/types/author-roles'

// return an object where keys are roles, and values are string[]
export function groupAuthorsByRole (authors: AuthorDto[]): any {
  return mapValues(groupBy(authors, 'role'),
    authors => authors.map((author: AuthorDto) => author.name))
}

// return an object where keys are roles (plural form), and values are string[]
export function groupAuthorsByRolePlural (authors: AuthorDto[]): any {
  const r = mapKeys(groupAuthorsByRole(authors),
    (v, k) => get(authorRoles.find(x => x.role === k), 'plural', k)
  )

  // sort object keys according to the order of keys in authorRoles
  // push unknown keys to the end of the array
  const roles = authorRoles.map(x => x.plural)
  const o = {} as any
  Object.keys(r)
    .sort((a, b) => {
      let index1 = roles.indexOf(a)
      let index2 = roles.indexOf(b)
      return (index1 === -1 ? Infinity : index1) - (index2 === -1 ? Infinity : index2)
    })
    .forEach((key: string) => {
      o[key] = r[key]
    })

  return o
}
