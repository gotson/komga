import {AxiosInstance} from 'axios'
import {OAuth2ClientDto} from '@/types/komga-oauth2'

const API_OAUTH2 = '/api/v1/oauth2'

export default class KomgaOauth2Service {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getProviders(): Promise<OAuth2ClientDto[]> {
    try {
      return (await this.http.get(`${API_OAUTH2}/providers`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve oauth2 providers'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
