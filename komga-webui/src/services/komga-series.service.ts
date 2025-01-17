import {AxiosInstance} from 'axios'
import {AuthorDto, BookDto} from '@/types/komga-books'
import {GroupCountDto, SeriesDto, SeriesMetadataUpdateDto, SeriesThumbnailDto} from '@/types/komga-series'
import {SeriesSearch} from '@/types/komga-search'

const qs = require('qs')

const API_SERIES = '/api/v1/series'

export default class KomgaSeriesService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getSeriesList(search: SeriesSearch, pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      return (await this.http.post(`${API_SERIES}/list`, search, {
        params: {...pageRequest},
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getSeriesListByAlphabeticalGroups(search: SeriesSearch): Promise<GroupCountDto[]>{
    try {
      return (await this.http.post(`${API_SERIES}/list/alphabetical-groups`, search)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve series alphabetical groups'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getNewSeries(libraryId?: string, oneshot?: boolean, pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      const params = {...pageRequest} as any
      if (libraryId) params.library_id = libraryId
      if (oneshot !== undefined) params.oneshot = oneshot
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

  async getUpdatedSeries(libraryId?: string, oneshot?: boolean, pageRequest?: PageRequest): Promise<Page<SeriesDto>> {
    try {
      const params = {...pageRequest} as any
      if (libraryId) params.library_id = libraryId
      if (oneshot !== undefined) params.oneshot = oneshot
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

  async getOneSeries(seriesId: string): Promise<SeriesDto> {
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

  async getCollections(seriesId: string): Promise<CollectionDto[]> {
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

  async analyzeSeries(series: SeriesDto) {
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

  async refreshMetadata(series: SeriesDto) {
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

  async updateMetadata(seriesId: string, metadata: SeriesMetadataUpdateDto) {
    try {
      await this.http.patch(`${API_SERIES}/${seriesId}/metadata`, metadata)
    } catch (e) {
      let msg = 'An error occurred while trying to update series metadata'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markAsRead(seriesId: string) {
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

  async markAsUnread(seriesId: string) {
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

  async getThumbnails(seriesId: string): Promise<SeriesThumbnailDto[]> {
    try {
      return (await this.http.get(`${API_SERIES}/${seriesId}/thumbnails`)).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve thumbnails for series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async uploadThumbnail(seriesId: string, file: File, selected: boolean) {
    try {
      const body = new FormData()
      body.append('file', file)
      body.append('selected', `${selected}`)
      await this.http.post(`${API_SERIES}/${seriesId}/thumbnails`, body)
    } catch (e) {
      let msg = `An error occurred while trying to upload thumbnail for series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteThumbnail(seriesId: string, thumbnailId: string) {
    try {
      await this.http.delete(`${API_SERIES}/${seriesId}/thumbnails/${thumbnailId}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete thumbnail for series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async markThumbnailAsSelected(seriesId: string, thumbnailId: string) {
    try {
      await this.http.put(`${API_SERIES}/${seriesId}/thumbnails/${thumbnailId}/selected`)
    } catch (e) {
      let msg = `An error occurred while trying to mark thumbnail as selected for series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteSeries(seriesId: string) {
    try {
      await this.http.delete(`${API_SERIES}/${seriesId}/file`)
    } catch (e) {
      let msg = `An error occurred while trying delete series '${seriesId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
