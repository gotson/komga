interface DirectoryListingDto {
  parent?: string,
  directories: PathDto[],
  files: PathDto[],
}

interface PathDto {
  type: string,
  name: string,
  path: string
}
