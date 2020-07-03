interface LibraryCreationDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean
}

interface LibraryUpdateDto {
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean
}

interface LibraryDto {
  id: number,
  name: string,
  root: string,
  importComicInfoBook: boolean,
  importComicInfoSeries: boolean,
  importComicInfoCollection: boolean,
  importEpubBook: boolean,
  importEpubSeries: boolean
}
