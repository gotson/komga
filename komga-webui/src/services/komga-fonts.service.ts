import {AxiosInstance} from 'axios'

const API_FONTS = '/api/v1/fonts'

export default class KomgaFontsService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getFamilies(): Promise<string[]> {
    try {
      return (await this.http.get(`${API_FONTS}/families`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve font families'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
