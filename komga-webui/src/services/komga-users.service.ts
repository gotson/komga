import {AxiosInstance} from 'axios'
import {
  ApiKeyDto,
  ApiKeyRequestDto,
  AuthenticationActivityDto,
  PasswordUpdateDto,
  UserCreationDto,
  UserDto,
  UserUpdateDto,
} from '@/types/komga-users'

const qs = require('qs')

const API_USERS = '/api/v2/users'

export default class KomgaUsersService {
  private http: AxiosInstance

  constructor(http: AxiosInstance) {
    this.http = http
  }

  async getMeWithAuth(login: string, password: string, rememberMe: boolean): Promise<UserDto> {
    try {
      return (await this.http.get(
        `${API_USERS}/me`,
        {
          auth: {
            username: login,
            password: password,
          },
          params: {
            'remember-me': rememberMe,
          },
        },
      )).data
    } catch (e) {
      let msg = 'An error occurred while trying to login'
      if (e.response) {
        if (e.response.status === 401) {
          msg = 'Invalid authentication'
        }
      }
      if (e.response?.data?.message) {
        msg += `: ${e.response.data.message}`
      } else {
        msg += `: ${e.message}`
      }
      throw new Error(msg)
    }
  }

  async getMe(): Promise<UserDto> {
    try {
      return (await this.http.get(`${API_USERS}/me`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve current user'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getAll(): Promise<UserDto[]> {
    try {
      return (await this.http.get(`${API_USERS}`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve all users'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async postUser(user: UserCreationDto): Promise<UserDto> {
    try {
      return (await this.http.post(API_USERS, user)).data
    } catch (e) {
      let msg = `An error occurred while trying to add user '${user.email}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async patchUser(userId: string, patch: UserUpdateDto) {
    try {
      await this.http.patch(`${API_USERS}/${userId}`, patch)
    } catch (e) {
      let msg = `An error occurred while trying to patch user '${userId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteUser(user: UserDto) {
    try {
      await this.http.delete(`${API_USERS}/${user.id}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete user '${user.email}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async patchUserPassword(user: UserDto, newPassword: PasswordUpdateDto) {
    try {
      await this.http.patch(`${API_USERS}/${user.id}/password`, newPassword)
    } catch (e) {
      let msg = `An error occurred while trying to update password for user ${user.email}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async logout() {
    try {
      await this.http.post('api/logout')
    } catch (e) {
      let msg = 'An error occurred while trying to logout'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getMyAuthenticationActivity(pageRequest?: PageRequest): Promise<Page<AuthenticationActivityDto>> {
    try {
      return (await this.http.get(`${API_USERS}/me/authentication-activity`, {
        params: pageRequest,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve authentication activity'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getAuthenticationActivity(pageRequest?: PageRequest): Promise<Page<AuthenticationActivityDto>> {
    try {
      return (await this.http.get(`${API_USERS}/authentication-activity`, {
        params: pageRequest,
        paramsSerializer: params => qs.stringify(params, {indices: false}),
      })).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve authentication activity'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getLatestAuthenticationActivityForUser(userId: string, apiKeyId?: string): Promise<AuthenticationActivityDto> {
    try {
      const params = {} as any
      if (apiKeyId) {
        params.apikey_id = apiKeyId
      }
      return (await this.http.get(`${API_USERS}/${userId}/authentication-activity/latest`, {params: params})).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve latest authentication activity for user ${userId}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getApiKeys(): Promise<ApiKeyDto[]> {
    try {
      return (await this.http.get(`${API_USERS}/me/api-keys`)).data
    } catch (e) {
      let msg = 'An error occurred while trying to retrieve api keys'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async createApiKey(apiKeyRequest: ApiKeyRequestDto): Promise<ApiKeyDto> {
    try {
      return (await this.http.post(`${API_USERS}/me/api-keys`, apiKeyRequest)).data
    } catch (e) {
      let msg = 'An error occurred while trying to create api key'
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteApiKey(apiKeyId: string) {
    try {
      await this.http.delete(`${API_USERS}/me/api-keys/${apiKeyId}`)
    } catch (e) {
      let msg = `An error occurred while trying to delete api key ${apiKeyId}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
