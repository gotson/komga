import { AxiosInstance } from 'axios'

const API_USERS = '/api/v1/users'

export default class KomgaUsersService {
  private http: AxiosInstance

  constructor (http: AxiosInstance) {
    this.http = http
  }

  async getMeWithAuth (login: string, password: string): Promise<UserDto> {
    try {
      return (await this.http.get(
        `${API_USERS}/me`,
        {
          auth: {
            username: login,
            password: password,
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
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async getMe (): Promise<UserDto> {
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

  async getAll (): Promise<UserWithSharedLibrariesDto[]> {
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

  async postUser (user: UserCreationDto): Promise<UserDto> {
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

  async patchUserRoles (userId: string, roles: RolesUpdateDto): Promise<UserDto> {
    try {
      return (await this.http.patch(`${API_USERS}/${userId}`, roles)).data
    } catch (e) {
      let msg = `An error occurred while trying to patch user '${userId}'`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async deleteUser (user: UserDto) {
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

  async patchMePassword (newPassword: PasswordUpdateDto) {
    try {
      return (await this.http.patch(`${API_USERS}/me/password`, newPassword)).data
    } catch (e) {
      let msg = `An error occurred while trying to update password for current user`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async patchUserSharedLibraries (user: UserDto, sharedLibrariesUpdateDto: SharedLibrariesUpdateDto) {
    try {
      return (await this.http.patch(`${API_USERS}/${user.id}/shared-libraries`, sharedLibrariesUpdateDto)).data
    } catch (e) {
      let msg = `An error occurred while trying to update shared libraries for user ${user.email}`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }

  async logout () {
    try {
      await this.http.post(`${API_USERS}/logout`)
    } catch (e) {
      let msg = `An error occurred while trying to logout`
      if (e.response.data.message) {
        msg += `: ${e.response.data.message}`
      }
      throw new Error(msg)
    }
  }
}
