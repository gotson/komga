import {AxiosInstance} from 'axios'
import {SettingsDto, SettingsUpdateDto} from '@/types/komga-settings'

const API_SETTINGS = '/api/v1/settings'

export default class KomgaSettingsService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getSettings(): Promise<SettingsDto> {
    try {
      return (await this.http.get(API_SETTINGS)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve settings'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async updateSettings(settings: SettingsUpdateDto) {
    try {
      await this.http.patch(API_SETTINGS, settings)
    } catch (e) {
      let msg = 'An error occurred while trying to update settings'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
