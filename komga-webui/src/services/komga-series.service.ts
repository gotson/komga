import { AxiosInstance } from 'axios'

const qs = require('qs')

const API_SERIES = '/api/v1/series'

export default class KomgaSeriesService {
  private http: AxiosInstance;

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getSeries (libraryId?: number, pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      return (await this.http.get(API_SERIES, {
        params: { library_id: libraryId, ...pageRequest },
        paramsSerializer: params => qs.stringify(params, { indices: false })
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve libraries'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
