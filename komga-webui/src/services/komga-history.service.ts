import {AxiosInstance} from 'axios'
import {HistoricalEventDto} from '@/types/komga-history'

const qs = require('qs')

const API_HISTORY = '/api/v1/history'

export default class KomgaHistoryService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getAll(pageRequest?: PageRequest): Promise<Page<HistoricalEventDto>> {
    try {
      return (await this.http.get(API_HISTORY, {
        params: pageRequest,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve historical events'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
