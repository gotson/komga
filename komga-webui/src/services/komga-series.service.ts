import { AxiosInstance } from 'axios'

const qs = require('qs')

const API_SERIES = '/api/v1/series'

export default class KomgaSeriesService {
  private http: AxiosInstance

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getSeries (libraryId?: string, pageRequest?: PageRequest, search?: string, status?: string[], readStatus?: string[]): Promise<Page<SeriesDto>> {
    try {
      const params = { ...pageRequest } as any
      if (libraryId) {
        params.library_id = libraryId
      }
      if (search) {
        params.search = search
      }
      if (status) {
        params.status = status
      }
      if (readStatus) {
        params.read_status = readStatus
      }
      return (await this.http.get(API_SERIES, {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false }),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getNewSeries (pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      const params = { ...pageRequest } as any
      return (await this.http.get(`${API_SERIES}/new`, {
        params: params,
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve new series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getUpdatedSeries (pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      const params = { ...pageRequest } as any
      return (await this.http.get(`${API_SERIES}/updated`, {
        params: params,
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve updated series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getOneSeries (seriesId: string): Promise<SeriesDto> {
    try {
      return (await this.http.get(`${API_SERIES}/${seriesId}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getBooks (seriesId: string, pageRequest?: PageRequest, readStatus?: string[]): Promise<Page<BookDto>> {
    try {
      const params = { ...pageRequest } as any
      if (readStatus) {
        params.read_status = readStatus
      }
      return (await this.http.get(`${API_SERIES}/${seriesId}/books`, {
        params: params,
        paramsSerializer: params => qs.stringify(params, { indices: false }),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve books'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getCollections (seriesId: string): Promise<CollectionDto[]> {
    try {
      return (await this.http.get(`${API_SERIES}/${seriesId}/collections`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve collections'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async analyzeSeries (series: SeriesDto) {
    try {
      await this.http.post(`${API_SERIES}/${series.id}/analyze`)
    } catch (e) {
      let msg = `An error occurred while trying to analyze series '${series.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async refreshMetadata (series: SeriesDto) {
    try {
      await this.http.post(`${API_SERIES}/${series.id}/metadata/refresh`)
    } catch (e) {
      let msg = `An error occurred while trying to refresh metadata for series '${series.name}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async updateMetadata (seriesId: string, metadata: SeriesMetadataUpdateDto) {
    try {
      await this.http.patch(`${API_SERIES}/${seriesId}/metadata`, metadata)
    } catch (e) {
      let msg = `An error occurred while trying to update series metadata`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markAsRead (seriesId: string) {
    try {
      await this.http.post(`${API_SERIES}/${seriesId}/read-progress`)
    } catch (e) {
      let msg = `An error occurred while trying to mark as read for series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markAsUnread (seriesId: string) {
    try {
      await this.http.delete(`${API_SERIES}/${seriesId}/read-progress`)
    } catch (e) {
      let msg = `An error occurred while trying to mark as unread for series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
