import {AxiosInstance} from 'axios'

const API_LOGIN = '/api/v1/login'

export default class KomgaLoginService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async setCookie(xAuthToken: string) {
    try {
      await this.http.get(`${API_LOGIN}/set-cookie`, {
        headers: {'X-Auth-Token': xAuthToken},
      })
    } catch (e) {
      let msg = 'An error occurred while trying to exchange xAuthToken for session cookie'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
