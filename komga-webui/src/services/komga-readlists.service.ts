import {AxiosInstance} from 'axios'
import {AuthorDto, BookDto} from '@/types/komga-books'
import {
  ReadListCreationDto,
  ReadListDto,
  ReadListRequestMatchDto,
  ReadListThumbnailDto,
  ReadListUpdateDto,
} from '@/types/komga-readlists'

const qs = require('qs')

const API_READLISTS = '/api/v1/readlists'

export default class KomgaReadListsService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getReadLists(libraryIds?: string[], pageRequest?: PageRequest, search?: string): Promise<Page<ReadListDto>> {
    try {
      const params = {...pageRequest} as any
      if (libraryIds) params.library_id = libraryIds
      if (search) params.search = search

      return (await this.http.get(API_READLISTS, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve readLists'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getOneReadList(readListId: string): Promise<ReadListDto> {
    try {
      return (await this.http.get(`${API_READLISTS}/${readListId}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve readList'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async postReadList(readList: ReadListCreationDto): Promise<ReadListDto> {
    try {
      return (await this.http.post(API_READLISTS, readList)).data
    } catch (e) {
      let msg = `An error occurred while trying to add readList '${readList.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async postReadListMatch(file: any): Promise<ReadListRequestMatchDto> {
    try {
      const formData = new FormData()
      formData.append('file', file)
      return (await this.http.post(`${API_READLISTS}/match/comicrack`, formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to match readlist'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async patchReadList(readListId: string, readList: ReadListUpdateDto) {
    try {
      await this.http.patch(`${API_READLISTS}/${readListId}`, readList)
    } catch (e) {
      let msg = `An error occurred while trying to update readList '${readListId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteReadList(readListId: string) {
    try {
      await this.http.delete(`${API_READLISTS}/${readListId}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete readList '${readListId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBooks(readListId: string, pageRequest?: PageRequest,
                 libraryId?: string[], readStatus?: string[],
                 tag?: string[], authors?: AuthorDto[]): Promise<Page<BookDto>> {
    try {
      const params = {...pageRequest} as any
      if (libraryId) params.library_id = libraryId
      if (readStatus) params.read_status = readStatus
      if (tag) params.tag = tag
      if (authors) params.author = authors.map(a => `${a.name},${a.role}`)

      return (await this.http.get(`${API_READLISTS}/${readListId}/books`, {
        params: params,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve books'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBookSiblingNext(readListId: string, bookId: string): Promise<BookDto> {
    try {
      return (await this.http.get(`${API_READLISTS}/${readListId}/books/${bookId}/next`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve book'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBookSiblingPrevious(readListId: string, bookId: string): Promise<BookDto> {
    try {
      return (await this.http.get(`${API_READLISTS}/${readListId}/books/${bookId}/previous`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve book'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getThumbnails(readListId: string): Promise<ReadListThumbnailDto[]> {
    try {
      return (await this.http.get(`${API_READLISTS}/${readListId}/thumbnails`)).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve thumbnails for readlist '${readListId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async uploadThumbnail(readListId: string, file: File, selected: boolean) {
    try {
      const body = new FormData()
      body.append('file', file)
      body.append('selected', `${selected}`)
      await this.http.post(`${API_READLISTS}/${readListId}/thumbnails`, body)
    } catch (e) {
      let msg = `An error occurred while trying to upload thumbnail for readlist '${readListId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteThumbnail(readListId: string, thumbnailId: string) {
    try {
      await this.http.delete(`${API_READLISTS}/${readListId}/thumbnails/${thumbnailId}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete thumbnail for readlist '${readListId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markThumbnailAsSelected(readListId: string, thumbnailId: string) {
    try {
      await this.http.put(`${API_READLISTS}/${readListId}/thumbnails/${thumbnailId}/selected`)
    } catch (e) {
      let msg = `An error occurred while trying to mark thumbnail as selected for readlist '${readListId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
