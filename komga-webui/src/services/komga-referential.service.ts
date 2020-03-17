import { AxiosInstance } from 'axios'

const qs = require('qs')

export default class KomgaReferentialService {
  private http: AxiosInstance

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getAuthors (search?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (search) {
        params.search = search
      }
      return (await this.http.get('/api/v1/authors', {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false })
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve authors'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
