export type ItemCardProps = {
  /**
   * Card width.
   *
   * Defaults to `150`.
   */
  width?: number
  /**
   * Disable card selection.
   */
  disableSelection?: boolean
  /**
   * Whether the card is currently selected.
   */
  selected?: boolean
  /**
   * State where the selection checkbox is shown, for instance when other items in the group have been selected already.
   */
  preSelect?: boolean
  /**
   * Whether to stretch the poster or not. If `false`, the image will have the `contain` property.
   *
   * Defaults to `true`.
   */
  stretchPoster?: boolean
}

export type ItemCardEmits = {
  selection: [selected: boolean, event: Event]
}

export type ItemCardTitle = {
  /**
   * Text to display.
   */
  text: string
  /**
   * Number of lines.
   */
  lines?: number
}

export type ItemCardLine = {
  /**
   * Text to display.
   */
  text?: string
  /**
   * Classes to apply.
   */
  classes?: string
  /**
   * Number of lines of text.
   */
  lines?: number
  /**
   * Whether the container will be shown even if `text` is empty.
   */
  allowEmpty?: boolean
}
