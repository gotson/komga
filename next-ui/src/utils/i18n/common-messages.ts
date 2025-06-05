import { defineMessage } from 'vue-intl'

export const commonMessages = {
  somethingWentWrongTitle: defineMessage({
    description: 'Common message: an error happened while loading data',
    defaultMessage: 'Something went wrong',
    id: 'ixQlWv',
  }),
  somethingWentWrongSubTitle: defineMessage({
    description: 'Common message: an error happened while loading data, explanation',
    defaultMessage: 'There might be a problem with your connection or your server.',
    id: 'hYO2n6',
  }),
}
