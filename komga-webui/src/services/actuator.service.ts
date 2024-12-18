import {AxiosInstance} from 'axios'

const API_ACTUATOR = '/actuator'

export default class ActuatorService {
  private http: AxiosInstance;

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getInfo (): Promise<ActuatorInfo> {
    try {
      return (await this.http.get(`${API_ACTUATOR}/info`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve actuator info'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async shutdown () {
    try {
      await this.http.post(`${API_ACTUATOR}/shutdown`)
    } catch (e) {
      let msg = 'An error occurred while trying to shutdown server'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  logfileUrl(): string {
    return `${API_ACTUATOR}/logfile`
  }
}
