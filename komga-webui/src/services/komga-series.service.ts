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
      const params = { ...pageRequest } as any
      if (libraryId) {
        params.library_id = libraryId
      }
      return (await this.http.get(API_SERIES, {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false })
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getOneSeries (seriesId: number): Promise<SeriesDto> {
    try {
      return (await this.http.get(`${API_SERIES}/${seriesId}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBooks (seriesId: number, pageRequest?: PageRequest): Promise<Page<BookDto>> {
    try {
      const data = (await this.http.get(`${API_SERIES}/${seriesId}/books`)).data
      data.content.forEach(function (b: any) {
        b.seriesId = seriesId
      })
      return data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve books'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
