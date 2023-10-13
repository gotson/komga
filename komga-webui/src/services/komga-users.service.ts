import {AxiosInstance} from 'axios'
import {
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

  async getLatestAuthenticationActivityForUser(user: UserDto): Promise<AuthenticationActivityDto> {
    try {
      return (await this.http.get(`${API_USERS}/${user.id}/authentication-activity/latest`)).data
    } catch (e) {
      let msg = `An error occurred while trying to retrieve latest authentication activity for user ${user.email}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
