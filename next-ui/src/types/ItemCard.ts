export type ItemCardProps = {
  /**
   * Card width.
   *
   * Defaults to `150`.
   */
  width?: string | number
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
}

export type ItemCardEmits = {
  selection: [selected: boolean]
}
