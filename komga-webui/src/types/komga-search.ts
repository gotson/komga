export interface SeriesSearch {
  condition?: SearchConditionSeries,
  fullTextSearch?: string,
}

export interface BookSearch {
  condition?: SearchConditionBook,
  fullTextSearch?: string,
}

export interface SearchConditionSeries {
}

export interface SearchConditionBook {
}

export class SearchConditionAnyOfBook implements SearchConditionBook {
  anyOf: SearchConditionBook[]

  constructor(conditions: SearchConditionBook[]) {
    this.anyOf = conditions
  }
}

export class SearchConditionAllOfBook implements SearchConditionBook {
  allOf: SearchConditionBook[]

  constructor(conditions: SearchConditionBook[]) {
    this.allOf = conditions
  }
}

export class SearchConditionAnyOfSeries implements SearchConditionSeries {
  anyOf: SearchConditionSeries[]

  constructor(conditions: SearchConditionSeries[]) {
    this.anyOf = conditions
  }
}

export class SearchConditionAllOfSeries implements SearchConditionSeries {
  allOf: SearchConditionSeries[]

  constructor(conditions: SearchConditionSeries[]) {
    this.allOf = conditions
  }
}

export class SearchConditionLibraryId implements SearchConditionBook, SearchConditionSeries {
  libraryId: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.libraryId = op
  }
}

export class SearchConditionSeriesId implements SearchConditionBook {
  seriesId: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.seriesId = op
  }
}

export class SearchConditionSeriesStatus implements SearchConditionSeries {
  seriesStatus: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.seriesStatus = op
  }
}

export class SearchConditionReadStatus implements SearchConditionBook, SearchConditionSeries {
  readStatus: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.readStatus = op
  }
}

export class SearchConditionMediaStatus implements SearchConditionBook {
  mediaStatus: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.mediaStatus = op
  }
}

export class SearchConditionMediaProfile implements SearchConditionBook {
  mediaProfile: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.mediaProfile = op
  }
}

export class SearchConditionGenre implements SearchConditionSeries {
  genre: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.genre = op
  }
}

export class SearchConditionTag implements SearchConditionBook, SearchConditionSeries {
  tag: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.tag = op
  }
}

export class SearchConditionLanguage implements SearchConditionSeries {
  language: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.language = op
  }
}

export class SearchConditionPublisher implements SearchConditionSeries {
  publisher: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.publisher = op
  }
}

export class SearchConditionAgeRating implements SearchConditionSeries {
  ageRating: SearchOperatorNumericNullable

  constructor(op: SearchOperatorNumericNullable) {
    this.ageRating = op
  }
}

export class SearchConditionReleaseDate implements SearchConditionBook, SearchConditionSeries {
  releaseDate: SearchOperatorDate

  constructor(op: SearchOperatorDate) {
    this.releaseDate = op
  }
}

export class SearchConditionSharingLabel implements SearchConditionBook, SearchConditionSeries {
  sharingLabel: SearchOperatorEquality

  constructor(op: SearchOperatorEquality) {
    this.sharingLabel = op
  }
}

export class SearchConditionComplete implements SearchConditionSeries {
  complete: SearchOperatorBoolean

  constructor(op: SearchOperatorBoolean) {
    this.complete = op
  }
}

export class SearchConditionOneShot implements SearchConditionBook, SearchConditionSeries {
  oneShot: SearchOperatorBoolean

  constructor(op: SearchOperatorBoolean) {
    this.oneShot = op
  }
}

export class SearchConditionAuthor implements SearchConditionBook, SearchConditionSeries {
  author: SearchOperatorBoolean

  constructor(op: SearchOperatorEquality) {
    this.author = op
  }
}

export class SearchConditionPoster implements SearchConditionBook {
  poster: SearchOperatorBoolean

  constructor(op: SearchOperatorEquality) {
    this.poster = op
  }
}

export class SearchConditionTitleSort implements SearchConditionSeries {
  titleSort: SearchOperatorString

  constructor(op: SearchOperatorString) {
    this.titleSort = op
  }
}

export interface AuthorMatch {
  name?: string,
  role?: string
}

export interface PosterMatch {
  type?: PosterMatchType,
  selected?: boolean
}

export enum PosterMatchType {
  GENERATED = 'GENERATED',
  SIDECAR = 'SIDECAR',
  USER_UPLOADED = 'USER_UPLOADED',
}

export interface SearchOperatorEquality {
}

export interface SearchOperatorBoolean {
}

export interface SearchOperatorNumericNullable {
}

export interface SearchOperatorDate {
}

export interface SearchOperatorString {
}

export class SearchOperatorIs implements SearchOperatorEquality, SearchOperatorNumericNullable, SearchOperatorDate {
  readonly operator: string = 'is'
  value: any

  constructor(value: any) {
    this.value = value
  }
}

export class SearchOperatorIsNot implements SearchOperatorEquality, SearchOperatorNumericNullable, SearchOperatorDate {
  readonly operator: string = 'isNot'
  value: any

  constructor(value: any) {
    this.value = value
  }
}

export class SearchOperatorBefore implements SearchOperatorDate {
  readonly operator: string = 'before'
  dateTime: any

  constructor(value: any) {
    this.dateTime = value
  }
}

export class SearchOperatorAfter implements SearchOperatorDate {
  readonly operator: string = 'after'
  dateTime: any

  constructor(value: any) {
    this.dateTime = value
  }
}

export class SearchOperatorIsNull implements SearchOperatorNumericNullable, SearchOperatorDate {
  readonly operator: string = 'isNull'
}

export class SearchOperatorIsNotNull implements SearchOperatorNumericNullable, SearchOperatorDate {
  readonly operator: string = 'isNotNull'
}

export class SearchOperatorIsTrue implements SearchOperatorBoolean {
  readonly operator: string = 'isTrue'
}

export class SearchOperatorIsFalse implements SearchOperatorBoolean {
  readonly operator: string = 'isFalse'
}

export class SearchOperatorBeginsWith implements SearchOperatorString {
  readonly operator: string = 'beginsWith'
  value: any

  constructor(value: any) {
    this.value = value
  }
}

export class SearchOperatorDoesNotBeginWith implements SearchOperatorString {
  readonly operator: string = 'doesNotBeginWith'
  value: any

  constructor(value: any) {
    this.value = value
  }
}
