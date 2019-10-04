interface DirectoryListingDto {
  parent?: string,
  directories: PathDto[]
}

interface PathDto {
  type: string,
  name: string,
  path: string
}
