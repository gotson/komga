import {AxiosInstance} from 'axios'

const API_RELEASES = '/api/v1/releases'

export default class KomgaReleasesService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getReleases(): Promise<ReleaseDto> {
    try {
      return (await this.http.get(API_RELEASES)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve releases'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
