import i18n from '@/i18n'

export function convertErrorCodes(message: string): string {
  const match = message.match(/ERR_\d{4}/g)
  let r = message
  if(match){
    match.forEach(x => r = r.replace(x, i18n.t(`error_codes.${x}`).toString()))
  }
  return r
}
