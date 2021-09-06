// those enums are client side only, we can use whatever values we want
// here we use the translation key
export enum ScaleType {
  SCREEN = 'bookreader.scale_type.screen',
  WIDTH = 'bookreader.scale_type.width',
  WIDTH_SHRINK_ONLY = 'bookreader.scale_type.width_shrink_only',
  HEIGHT = 'bookreader.scale_type.height',
  ORIGINAL = 'bookreader.scale_type.original'
}

export enum ContinuousScaleType {
  WIDTH = 'bookreader.scale_type.continuous_width',
  ORIGINAL = 'bookreader.scale_type.continuous_original'
}

export enum PagedReaderLayout {
  SINGLE_PAGE = 'bookreader.paged_reader_layout.single',
  DOUBLE_PAGES = 'bookreader.paged_reader_layout.double',
  DOUBLE_NO_COVER = 'bookreader.paged_reader_layout.double_no_cover'
}

export const PaddingPercentage: number[] = [0, 5, 10, 15, 20, 25, 30, 35, 40]
