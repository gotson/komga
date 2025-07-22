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
  networkError: defineMessage({
    description: 'Common message: a network error happened when communicating with the server',
    defaultMessage: 'Network error',
    id: 'Z/EY89',
  }),
  error: defineMessage({
    description: 'Common message: an unknown error happened',
    defaultMessage: 'error',
    id: 'HOBFqq',
  }),
  selectItemOrCreateOne: defineMessage({
    description: 'Common message: shown in combobox',
    defaultMessage: 'Select an item or create one',
    id: 'HXms0S',
  }),
  changePasswordDialogTitle: defineMessage({
    description: 'Change Password dialog title',
    defaultMessage: 'Change Password',
    id: 'dHyAgE',
  }),
}
