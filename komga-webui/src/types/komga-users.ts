interface UserDto {
  id: string,
  email: string,
  roles: string[]
}

interface UserWithSharedLibrariesDto {
  id: string,
  email: string,
  roles: string[],
  sharedAllLibraries: boolean,
  sharedLibraries: SharedLibraryDto[]
}

interface SharedLibraryDto {
  id: string
}

interface UserCreationDto {
  email: string,
  roles: string[]
}

interface PasswordUpdateDto {
  password: string
}

interface SharedLibrariesUpdateDto {
  all: boolean,
  libraryIds: string[]
}

interface RolesUpdateDto {
  roles: string[]
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
