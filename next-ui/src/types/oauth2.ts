type OAuthProviderConfig = {
  bgColor: string
  textColor: string
  icon?: string
}

export const oauth2ProviderStyles: Record<string, OAuthProviderConfig> = {
  google: {
    bgColor: '#ffffff',
    textColor: '#3c4043',
    icon: 'i-mdi:google',
  },
  github: {
    bgColor: '#24292e',
    textColor: '#ffffff',
    icon: 'i-mdi:github',
  },
  discord: {
    bgColor: '#5865f2',
    textColor: '#ffffff',
    icon: 'i-simple-icons:discord',
  },
  authentik: {
    bgColor: '#fd4b2d',
    textColor: '#ffffff',
    icon: 'i-simple-icons:authentik',
  },
  keycloak: {
    bgColor: '#1f2937',
    textColor: '#ffffff',
    icon: 'i-simple-icons:keycloak',
  },
  facebook: {
    bgColor: '#1877f2',
    textColor: '#ffffff',
    icon: 'i-mdi:facebook',
  },
  authelia: {
    bgColor: '#22242b',
    textColor: '#ffffff',
    icon: 'i-simple-icons:authelia',
  },
  voidauth: {
    bgColor: '#0f172a',
    textColor: '#ffffff',
  },
}
