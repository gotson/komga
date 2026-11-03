import { defineMessage } from 'vue-intl'

export const themes = [
  {
    value: 'light',
    message: defineMessage({
      description: 'Theme: light',
      defaultMessage: 'Light',
      id: 'UxBl+J',
    }),
    icon: 'i-mdi:weather-sunny',
  },
  {
    value: 'dark',
    message: defineMessage({
      description: 'Theme: dark',
      defaultMessage: 'Dark',
      id: '6cwDEs',
    }),
    icon: 'i-mdi:weather-night',
  },
  {
    value: 'system',
    message: defineMessage({
      description: 'Theme: system',
      defaultMessage: 'System',
      id: 'gjlnkj',
    }),
    icon: 'i-mdi:theme-light-dark',
  },
] as const
