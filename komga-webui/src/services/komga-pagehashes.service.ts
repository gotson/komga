import {AxiosInstance} from 'axios'
import {
  PageHashCreationDto,
  PageHashDto,
  PageHashKnownDto,
  PageHashMatchDto,
  PageHashUnknownDto,
} from '@/types/komga-pagehashes'

const qs = require('qs')

const API_PAGE_HASH = '/api/v1/page-hashes'

export default class KomgaPageHashesService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getKnownHashes(actions: string[], pageRequest?: PageRequest): Promise<Page<PageHashKnownDto>> {
    try {
      const params = {...pageRequest} as any
      if (actions) params.action = actions
      return (await this.http.get(API_PAGE_HASH, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve known page hashes'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getUnknownHashes(pageRequest?: PageRequest): Promise<Page<PageHashUnknownDto>> {
    try {
      return (await this.http.get(`${API_PAGE_HASH}/unknown`, {
        params: pageRequest,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve unknown page hashes'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getPageHashMatches(pageHash: PageHashDto, pageRequest?: PageRequest): Promise<Page<PageHashMatchDto>> {
    try {
      const params = {
        ...pageRequest,
        media_type: pageHash.mediaType,
        file_size: pageHash.size || -1,
      }
      return (await this.http.get(`${API_PAGE_HASH}/${pageHash.hash}`, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve matches for page hash: ${pageHash}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async createOrUpdatePageHash(pageHash: PageHashCreationDto) {
    try {
      await this.http.put(API_PAGE_HASH, pageHash)
    } catch (e) {
      let msg = `An error occurred while trying to add page hash ${pageHash}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async performDelete(pageHash: PageHashKnownDto) {
    try {
      const params = {
        media_type: pageHash.mediaType,
        file_size: pageHash.size || -1,
      }
      await this.http.post(`${API_PAGE_HASH}/${pageHash.hash}/perform-delete`, pageHash, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })
    } catch (e) {
      let msg = `An error occurred while trying to execute perform-delete on page hash ${pageHash}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
