import {AxiosInstance} from 'axios'
import {AuthorDto} from '@/types/komga-books'
import {NameValue} from '@/types/filter'

const qs = require('qs')
const tags = require('language-tags')

export default class KomgaReferentialService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getAuthors(search?: string, role?: string, libraryIds?: string[], collectionId?: string, seriesId?: string, readListId?: string): Promise<Page<AuthorDto>> {
    try {
      const params = {} as any
      if (search) params.search = search
      if (role) params.role = role
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId
      if (seriesId) params.series_id = seriesId
      if (readListId) params.readlist_id = readListId

      return (await this.http.get('/api/v2/authors', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve authors'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getAuthorsNames(search?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (search) {
        params.search = search
      }
      return (await this.http.get('/api/v1/authors/names', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve authors names'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getGenres(libraryIds?: string[], collectionId?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      return (await this.http.get('/api/v1/genres', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve genres'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getTags(): Promise<string[]> {
    try {
      return (await this.http.get('/api/v1/tags')).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve tags'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getSharingLabels(libraryIds?: string[], collectionId?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      return (await this.http.get('/api/v1/sharing-labels', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve sharing labels'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getSeriesAndBookTags(libraryIds?: string[], collectionId?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      return (await this.http.get('/api/v1/tags', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series and book tags'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBookTags(seriesId?: string, readListId?: string, libraryIds?: string[]): Promise<string[]> {
    try {
      const params = {} as any
      if (seriesId) params.series_id = seriesId
      if (readListId) params.readlist_id = readListId
      if (libraryIds) params.library_id = libraryIds

      return (await this.http.get('/api/v1/tags/book', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve book tags'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getPublishers(libraryIds?: string[], collectionId?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      return (await this.http.get('/api/v1/publishers', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve publishers'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getAgeRatings(libraryIds?: string[], collectionId?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      return (await this.http.get('/api/v1/age-ratings', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve age ratings'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getSeriesReleaseDates(libraryIds?: string[], collectionId?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      return (await this.http.get('/api/v1/series/release-dates', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series release dates'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getLanguages(libraryIds?: string[], collectionId?: string): Promise<NameValue[]> {
    try {
      const params = {} as any
      if (libraryIds) params.library_id = libraryIds
      if (collectionId) params.collection_id = collectionId

      const data = (await this.http.get('/api/v1/languages', {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
      const ret = [] as NameValue[]
      for (const code of data) {
        const tag = tags(code)
        if (tag.valid()) {
          const name = tag.language().descriptions()[0] + ` (${code})`
          ret.push({name: name, value: code})
        }
      }
      return ret
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve languages'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
