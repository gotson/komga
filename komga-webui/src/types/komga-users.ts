interface UserDto {
  id: number,
  email: string,
  roles: string[]
}

interface UserCreationDto {
  email: string,
  roles: string[]
}

interface PasswordUpdateDto {
  password: string
}
