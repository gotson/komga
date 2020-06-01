interface UserDto {
  id: number,
  email: string,
  roles: string[]
}

interface UserWithSharedLibrariesDto {
  id: number,
  email: string,
  roles: string[],
  sharedAllLibraries: boolean,
  sharedLibraries: SharedLibraryDto[]
}

interface SharedLibraryDto {
  id: number
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
  libraryIds: number[]
}
