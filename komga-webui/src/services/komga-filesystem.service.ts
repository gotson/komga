import { AxiosInstance } from 'axios'

const API_FILESYSTEM = '/api/v1/filesystem'

export default class KomgaFilesystemService {
  private http: AxiosInstance;

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getDirectoryListing (path: String = ''): Promise<DirectoryListingDto> {
    return (await this.http.get(API_FILESYSTEM, {
      params: {
        path: path
      }
    })).data
  }
}
