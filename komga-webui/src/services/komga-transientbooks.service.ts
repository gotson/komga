import {AxiosInstance} from 'axios'
import {TransientBookDto} from '@/types/komga-transientbooks'

const API_TRANSIENT_BOOKS = '/api/v1/transient-books'

export default class KomgaTransientBooksService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async scanForTransientBooks(path: string): Promise<TransientBookDto[]> {
    try {
      return (await this.http.post(API_TRANSIENT_BOOKS, {
        path: path,
      })).data
    } catch (e) {
      if (e.response.data.message) throw new Error(e.response.data.message)
      throw new Error('An error occurred while trying to scan for transient book')
    }
  }

  async analyze(id: string): Promise<TransientBookDto> {
    try {
      return (await this.http.post(`${API_TRANSIENT_BOOKS}/${id}/analyze`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to analyze transient book'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

}
