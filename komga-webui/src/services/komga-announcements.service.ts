import {AxiosInstance} from 'axios'
import {JsonFeedDto} from '@/types/json-feed'

const API_ANNOUNCEMENTS = '/api/v1/announcements'

export default class KomgaAnnouncementsService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getAnnouncements(): Promise<JsonFeedDto> {
    try {
      return (await this.http.get(API_ANNOUNCEMENTS)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve announcements'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markAnnouncementsRead(announcementIds: string[]) {
    try {
      await this.http.put(API_ANNOUNCEMENTS, announcementIds)
    } catch (e) {
      let msg = 'An error occurred while trying to mark announcements as read'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
