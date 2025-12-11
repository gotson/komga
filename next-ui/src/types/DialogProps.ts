type DialogBaseProps = {
  title?: string
  subtitle?: string
  maxWidth?: string | number
  activator?: Element | string
  loading?: boolean
  fullscreen?: boolean
  scrollable?: boolean
  shown?: boolean
}

type DialogConfirmBaseProps = DialogBaseProps & {
  okText?: string
  closeOnSave?: boolean
}

export type DialogSimpleProps = DialogBaseProps

export type DialogConfirmProps = DialogConfirmBaseProps & {
  validateText?: string
}

export type DialogConfirmEditProps = DialogConfirmBaseProps & {
  cardTextClass?: string
}
