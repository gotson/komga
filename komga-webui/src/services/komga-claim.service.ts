import { AxiosInstance } from 'axios'
import {UserDto} from '@/types/komga-users'

const API_CLAIM = '/api/v1/claim'

export default class KomgaClaimService {
  private http: AxiosInstance

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getClaimStatus (): Promise<ClaimStatus> {
    try {
      return (await this.http.get(API_CLAIM)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve claim status'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async claimServer (user: ClaimAdmin): Promise<UserDto> {
    try {
      return (await this.http.post(API_CLAIM, {}, {
        headers: {
          'X-Komga-Email': user.email,
          'X-Komga-Password': user.password,
        },
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to claim server'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
