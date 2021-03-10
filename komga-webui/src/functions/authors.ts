import {groupBy, mapValues} from 'lodash'
import {AuthorDto} from '@/types/komga-books'

// return an object where keys are roles, and values are string[]
export function groupAuthorsByRole (authors: AuthorDto[]): any {
  return mapValues(groupBy(authors, 'role'),
    authors => authors.map((author: AuthorDto) => author.name))
}
