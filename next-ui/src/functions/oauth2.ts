import { oauth2LoginUrl } from '@/api/links'

export function performOAuth2LoginRedirect(registrationId: string | undefined) {
  const target = oauth2LoginUrl(registrationId)
  if (target) window.location.assign(target)
}
