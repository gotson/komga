import {AxiosInstance} from 'axios'
import {SettingsDto, SettingsUpdateDto} from '@/types/komga-settings'
import {ClientSettingDto, ClientSettingGlobalUpdateDto, ClientSettingUserUpdateDto} from '@/types/komga-clientsettings'

const API_SETTINGS = '/api/v1/settings'
const API_CLIENT_SETTINGS = '/api/v1/client-settings'

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

  async getClientSettings(): Promise<ClientSettingDto[]> {
    try {
      return (await this.http.get(`${API_CLIENT_SETTINGS}/list`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve client settings'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async updateClientSettingGlobal(setting: ClientSettingGlobalUpdateDto) {
    try {
      await this.http.put(`${API_CLIENT_SETTINGS}/global`, setting)
    } catch (e) {
      let msg = 'An error occurred while trying to update global client setting'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async updateClientSettingUser(setting: ClientSettingUserUpdateDto) {
    try {
      await this.http.put(`${API_CLIENT_SETTINGS}/user`, setting)
    } catch (e) {
      let msg = 'An error occurred while trying to update user client setting'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
