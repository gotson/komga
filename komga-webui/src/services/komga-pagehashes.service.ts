import {AxiosInstance} from 'axios'
import {PageHashMatchDto, PageHashUnknownDto} from '@/types/komga-pagehashes'

const qs = require('qs')

const API_PAGE_HASH = '/api/v1/page-hashes'

export default class KomgaPageHashesService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
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

  async getUnknownPageHashMatches(hash: PageHashUnknownDto, pageRequest?: PageRequest): Promise<Page<PageHashMatchDto>> {
    try {
      const params = {
        ...pageRequest,
        media_type: hash.mediaType,
        size: hash.sizeBytes || -1,
      }
      return (await this.http.get(`${API_PAGE_HASH}/unknown/${hash.hash}`, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve matches for page hash: ${hash}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
