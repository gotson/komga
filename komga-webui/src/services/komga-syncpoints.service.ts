import {AxiosInstance} from 'axios'

const qs = require('qs')

const API_SYNCPOINTS = '/api/v1/syncpoints'

export default class KomgaSyncPointsService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async deleteMySyncPointsByApiKey(apiKeyId: string) {
    try {
      await this.http.delete(`${API_SYNCPOINTS}/me`, {
        params: {
          key_id: apiKeyId,
        },
      })
    } catch (e) {
      let msg = `An error occurred while trying to delete syncpoints for apikey '${apiKeyId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
