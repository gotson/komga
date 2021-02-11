import {groupBy, mapKeys, mapValues} from 'lodash'
import {authorRoles} from '@/types/author-roles'
import {AuthorDto} from '@/types/komga-books'
import i18n from "@/i18n";

// return an object where keys are roles, and values are string[]
export function groupAuthorsByRole (authors: AuthorDto[]): any {
  return mapValues(groupBy(authors, 'role'),
    authors => authors.map((author: AuthorDto) => author.name))
}

// return an object where keys are roles (plural form), and values are string[]
export function groupAuthorsByRolePlural (authors: AuthorDto[]): any {
  const r = mapKeys(groupAuthorsByRole(authors),
    (v, k) => i18n.t(`author_roles.${k}`),
  )

  // sort object keys according to the order of keys in authorRoles
  // push unknown keys to the end of the array
  const roles = authorRoles.map(x => i18n.t(`author_roles.${x.role}`))
  const o = {} as any
  Object.keys(r)
    .sort((a, b) => {
      const index1 = roles.indexOf(a)
      const index2 = roles.indexOf(b)
      return (index1 === -1 ? Infinity : index1) - (index2 === -1 ? Infinity : index2)
    })
    .forEach((key: string) => {
      o[key] = r[key]
    })

  return o
}
