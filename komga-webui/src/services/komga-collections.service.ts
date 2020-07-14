import { AxiosInstance } from 'axios'

const qs = require('qs')

const API_COLLECTIONS = '/api/v1/collections'

export default class KomgaCollectionsService {
  private http: AxiosInstance

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getCollections (libraryIds?: string[], pageRequest?: PageRequest, search?: string): Promise<Page<CollectionDto>> {
    try {
      const params = { ...pageRequest } as any
      if (libraryIds) params.library_id = libraryIds
      if (search) params.search = search

      return (await this.http.get(API_COLLECTIONS, {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false }),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve collections'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getOneCollection (collectionId: string): Promise<CollectionDto> {
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

  async postCollection (collection: CollectionCreationDto): Promise<CollectionDto> {
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

  async patchCollection (collectionId: string, collection: CollectionUpdateDto) {
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

  async deleteCollection (collectionId: string) {
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

  async getSeries (collectionId: string, pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      const params = { ...pageRequest }
      return (await this.http.get(`${API_COLLECTIONS}/${collectionId}/series`, {
        params: params,
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
