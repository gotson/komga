type DialogBaseProps = {
  /**
   * Title of the dialog.
   */
  title?: string
  /**
   * Subtitle of the dialog.
   */
  subtitle?: string
  /**
   * Maximum width of the dialog.
   */
  maxWidth?: string | number
  /**
   * Activator for the dialog.
   */
  activator?: Element | string
  /**
   * Loading indicator, applies to the dialog's card.
   */
  loading?: boolean
  /**
   * Whether the dialog should be displayed in full screen.
   */
  fullscreen?: boolean
  /**
   * Whether the dialog is scrollable.
   */
  scrollable?: boolean
  /**
   * Controls the dialog's visibility.
   */
  shown?: boolean
}

type DialogConfirmBaseProps = DialogBaseProps & {
  /**
   * Text for the confirmation button.
   */
  okText?: string
  /**
   * Whether the dialog should close itself on save.
   * If disabled, the dialog should be closed inside the callback function.
   */
  closeOnSave?: boolean
}

export type DialogSimpleProps = DialogBaseProps

export type DialogConfirmProps = DialogConfirmBaseProps & {
  /**
   * Text that needs to be typed to validate. Only shown when `mode` is set to `textinput`.
   */
  validateText?: string
  /**
   * Label shown next to the confirmation checkbox. Only shown when `mode` is set to `checkbox`.
   */
  checkboxLabel?: string
  /**
   * Confirmation mechanism:
   * - `textinput`: requires typing the content of 'validateText' into a text field.
   * - `checkbox`: requires checking a checkbox.
   * - `click`: no extra validation, just click on the confirm button of the dialog.
   */
  mode?: 'textinput' | 'checkbox' | 'click'
  /**
   * Color used for the checkbox and confirm button. Defaults to `error`.
   */
  color?: string
}

export type DialogConfirmEditProps = DialogConfirmBaseProps & {
  /**
   * CSS classes applied to the card.
   */
  cardTextClass?: string
}
