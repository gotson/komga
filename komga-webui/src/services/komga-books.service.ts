import { AxiosInstance } from 'axios'

const qs = require('qs')

const API_BOOKS = '/api/v1/books'

export default class KomgaBooksService {
  private http: AxiosInstance;

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getBooks (libraryId?: number, pageRequest?: PageRequest): Promise<Page<BookDto>> {
    try {
      const params = { ...pageRequest } as any
      if (libraryId) {
        params.library_id = libraryId
      }
      return (await this.http.get(API_BOOKS, {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false })
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve books'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBook (bookId: number): Promise<BookDto> {
    try {
      return (await this.http.get(`${API_BOOKS}/${bookId}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve book'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
