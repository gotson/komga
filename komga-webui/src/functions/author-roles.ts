import {authorRoles} from '@/types/author-roles'
import {BookDto} from '@/types/komga-books'

export function getCustomRoles(books: BookDto[]): string[] {
    return books.flatMap((b) => b.metadata.authors.map((a) => a.role)).filter((ra) => !authorRoles.includes(ra))
}

export function getCustomRolesForSeries(books: BookDto[], seriesId: string): string[] {
    return getCustomRoles(books.filter((b) => b.seriesId === seriesId))
}

export function isAllSelectedSameSeries(books: BookDto[]): boolean {
    return books.every((b) => b.seriesId === books[0].seriesId)
}