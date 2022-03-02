interface UserDto {
  id: string,
  email: string,
  roles: string[],
  sharedAllLibraries: boolean,
  sharedLibrariesIds: string[]
}

interface UserCreationDto {
  email: string,
  roles: string[]
}

interface PasswordUpdateDto {
  password: string
}

interface UserUpdateDto {
  roles?: string[],
  sharedLibraries?: {
    all: boolean,
    libraryIds: string[]
  },
}

interface AuthenticationActivityDto {
  userId?: string,
  email?: string,
  ip?: string,
  userAgent?: string,
  success: Boolean,
  error?: string,
  dateTime: string,
  source?: string,
}
