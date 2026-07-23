import {flatMap, groupBy, intersection, mapValues, reduce, uniq} from 'lodash'
import {AuthorDto, BookDto} from '@/types/komga-books'

type AuthorsByRole = {[role: string]: string[]}

// return an object where keys are roles, and values are string[]
export function groupAuthorsByRole (authors: AuthorDto[]): AuthorsByRole {
  return mapValues(groupBy(authors, 'role'),
    authors => authors.map((author: AuthorDto) => author.name))
}

// create an object where keys are roles and values are arrays of authors
// we're using intersection to only include authors that are present on all books of each roles
export function buildManyAuthorsByRole(books: BookDto[]): AuthorsByRole {
  const allAuthorsByRoles = books.map(book => groupAuthorsByRole(book.metadata.authors))
  const roleKeys = uniq(flatMap(allAuthorsByRoles, Object.keys))
  return reduce(roleKeys, (acc: {[key: string]: string[]}, key) => {
    const values = allAuthorsByRoles.map(authorsByRole => authorsByRole[key] || [])
    const intersect = intersection(...values.filter(arr => arr.length > 0))

    if (intersect.length > 0) {
      acc[key] = intersect
    }
    return acc
  }, {})
}
