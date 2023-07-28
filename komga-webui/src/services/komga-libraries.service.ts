import {AxiosInstance} from 'axios'
import {LibraryCreationDto, LibraryDto, LibraryUpdateDto} from '@/types/komga-libraries'

const API_LIBRARIES = '/api/v1/libraries'

export default class KomgaLibrariesService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getLibraries(): Promise<LibraryDto[]> {
    try {
      return (await this.http.get(API_LIBRARIES)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve libraries'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getLibrary(libraryId: string): Promise<LibraryDto> {
    try {
      return (await this.http.get(`${API_LIBRARIES}/${libraryId}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve library'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async postLibrary(library: LibraryCreationDto): Promise<LibraryDto> {
    try {
      return (await this.http.post(API_LIBRARIES, library)).data
    } catch (e) {
      let msg = `An error occurred while trying to add library '${library.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async updateLibrary(libraryId: string, library: LibraryUpdateDto) {
    try {
      await this.http.patch(`${API_LIBRARIES}/${libraryId}`, library)
    } catch (e) {
      let msg = `An error occurred while trying to update library '${libraryId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteLibrary(library: LibraryDto) {
    try {
      await this.http.delete(`${API_LIBRARIES}/${library.id}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete library '${library.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async scanLibrary(library: LibraryDto, scanDeep: boolean = false) {
    try {
      await this.http.post(`${API_LIBRARIES}/${library.id}/scan`, null, {
        params: {deep: scanDeep},
      })
    } catch (e) {
      let msg = `An error occurred while trying to scan library '${library.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async analyzeLibrary(library: LibraryDto) {
    try {
      await this.http.post(`${API_LIBRARIES}/${library.id}/analyze`)
    } catch (e) {
      let msg = `An error occurred while trying to analyze library '${library.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async refreshMetadata(library: LibraryDto) {
    try {
      await this.http.post(`${API_LIBRARIES}/${library.id}/metadata/refresh`)
    } catch (e) {
      let msg = `An error occurred while trying to refresh metadata for library '${library.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async emptyTrash(library: LibraryDto) {
    try {
      await this.http.post(`${API_LIBRARIES}/${library.id}/empty-trash`)
    } catch (e) {
      let msg = `An error occurred while trying to empty trash for library '${library.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
