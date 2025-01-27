import {AllowExclude} from '@/types/enum-users'

export interface UserDto {
  id: string,
  email: string,
  roles: string[],
  sharedAllLibraries: boolean,
  sharedLibrariesIds: string[],
  labelsAllow: string[],
  labelsExclude: string[],
  ageRestriction?: {
    age: number,
    restriction: AllowExclude,
  },
}

export interface UserCreationDto {
  email: string,
  roles: string[]
}

export interface PasswordUpdateDto {
  password: string
}

export interface UserUpdateDto {
  roles?: string[],
  sharedLibraries?: {
    all: boolean,
    libraryIds: string[]
  },
  ageRestriction?: {
    age: number,
    restriction: AllowExclude,
  }
  labelsAllow?: string[],
  labelsExclude?: string[],
}

export interface AuthenticationActivityDto {
  userId?: string,
  email?: string,
  apiKeyId?: string,
  apiKeyComment?: string,
  ip?: string,
  userAgent?: string,
  success: Boolean,
  error?: string,
  dateTime: Date,
  source?: string,
}

export interface ApiKeyDto {
  id: string,
  userId: string,
  key: string,
  comment: string,
  createdDate: Date,
  lastModifiedDate: Date
}

export interface ApiKeyRequestDto {
  comment: string,
}
