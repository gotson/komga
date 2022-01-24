import {AxiosInstance} from 'axios'
import {SeriesDto} from '@/types/komga-series'
import {AuthorDto} from '@/types/komga-books'

const qs = require('qs')

const API_COLLECTIONS = '/api/v1/collections'

export default class KomgaCollectionsService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getCollections(libraryIds?: string[], pageRequest?: PageRequest, search?: string): Promise<Page<CollectionDto>> {
    try {
      const params = {...pageRequest} as any
      if (libraryIds) params.library_id = libraryIds
      if (search) params.search = search

      return (await this.http.get(API_COLLECTIONS, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve collections'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getOneCollection(collectionId: string): Promise<CollectionDto> {
    try {
      return (await this.http.get(`${API_COLLECTIONS}/${collectionId}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve collection'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async postCollection(collection: CollectionCreationDto): Promise<CollectionDto> {
    try {
      return (await this.http.post(API_COLLECTIONS, collection)).data
    } catch (e) {
      let msg = `An error occurred while trying to add collection '${collection.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async patchCollection(collectionId: string, collection: CollectionUpdateDto) {
    try {
      await this.http.patch(`${API_COLLECTIONS}/${collectionId}`, collection)
    } catch (e) {
      let msg = `An error occurred while trying to update collection '${collectionId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteCollection(collectionId: string) {
    try {
      await this.http.delete(`${API_COLLECTIONS}/${collectionId}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete collection '${collectionId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getSeries(collectionId: string, pageRequest?: PageRequest,
                  libraryId?: string[], status?: string[],
                  readStatus?: string[], genre?: string[], tag?: string[], language?: string[],
                  publisher?: string[], ageRating?: string[], releaseDate?: string[], authors?: AuthorDto[],
                  complete?: boolean): Promise<Page<SeriesDto>> {
    try {
      const params = {...pageRequest} as any
      if (libraryId) params.library_id = libraryId
      if (status) params.status = status
      if (readStatus) params.read_status = readStatus
      if (genre) params.genre = genre
      if (tag) params.tag = tag
      if (language) params.language = language
      if (publisher) params.publisher = publisher
      if (ageRating) params.age_rating = ageRating
      if (releaseDate) params.release_year = releaseDate
      if (authors) params.author = authors.map(a => `${a.name},${a.role}`)
      if (complete !== undefined) params.complete = complete

      return (await this.http.get(`${API_COLLECTIONS}/${collectionId}/series`, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve series for collection '${collectionId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getThumbnails(collectionId: string): Promise<CollectionThumbnailDto[]> {
    try {
      return (await this.http.get(`${API_COLLECTIONS}/${collectionId}/thumbnails`)).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve thumbnails for collection '${collectionId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async uploadThumbnail(collecitonId: string, file: File, selected: boolean) {
    try {
      const body = new FormData()
      body.append('file', file)
      body.append('selected', `${selected}`)
      await this.http.post(`${API_COLLECTIONS}/${collecitonId}/thumbnails`, body)
    } catch (e) {
      let msg = `An error occurred while trying to upload thumbnail for collection '${collecitonId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteThumbnail(collectionId: string, thumbnailId: string) {
    try {
      await this.http.delete(`${API_COLLECTIONS}/${collectionId}/thumbnails/${thumbnailId}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete thumbnail for collection '${collectionId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markThumbnailAsSelected(collectionId: string, thumbnailId: string) {
    try {
      await this.http.put(`${API_COLLECTIONS}/${collectionId}/thumbnails/${thumbnailId}/selected`)
    } catch (e) {
      let msg = `An error occurred while trying to mark thumbnail as selected for collection '${collectionId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
