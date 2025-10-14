import { useIntl } from 'vue-intl'
import { errorCodeMessages } from '@/utils/i18n/enum/error-codes'

export function useErrorCodeFormatter() {
  const intl = useIntl()

  function convertErrorCodes(message?: string): string {
    if (!message) return ''
    const match = message.match(/ERR_\d{4}/g)
    let r = message
    match?.forEach((errorCode) => {
      if (errorCodeMessages[errorCode])
        r = r.replace(errorCode, intl.formatMessage(errorCodeMessages[errorCode]))
    })
    return r
  }

  return { convertErrorCodes }
}
