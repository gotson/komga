import { AxiosInstance } from 'axios'

const qs = require('qs')
const tags = require('language-tags')

export default class KomgaReferentialService {
  private http: AxiosInstance

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getAuthors (search?: string): Promise<string[]> {
    try {
      const params = {} as any
      if (search) {
        params.search = search
      }
      return (await this.http.get('/api/v1/authors', {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false }),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve authors'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getGenres (): Promise<string[]> {
    try {
      return (await this.http.get('/api/v1/genres')).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve genres'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getTags (): Promise<string[]> {
    try {
      return (await this.http.get('/api/v1/tags')).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve tags'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getPublishers (): Promise<string[]> {
    try {
      return (await this.http.get('/api/v1/publishers')).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve publishers'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getLanguages (): Promise<NameValue[]> {
    try {
      const data = (await this.http.get('/api/v1/languages')).data
      const ret = [] as NameValue[]
      for (const code of data) {
        const tag = tags(code)
        if (tag.valid()) {
          const name = tag.language().descriptions()[0] + ` (${code})`
          ret.push({ name: name, value: code })
        }
      }
      return ret
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve publishers'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
