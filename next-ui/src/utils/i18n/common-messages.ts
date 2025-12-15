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
  resourceIntensive: defineMessage({
    description: 'Resource intensive analysis warning',
    defaultMessage: 'Can consume lots of resources on large libraries or slow hardware',
    id: 'uoc99F',
  }),
  dialogEmptyTrashTitle: defineMessage({
    description: 'Library empty trash dialog: title',
    defaultMessage: 'Empty trash',
    id: 'ELttw/',
  }),
  dialogEmptyTrashConfirm: defineMessage({
    description: 'Library empty trash dialog: confirm button',
    defaultMessage: 'Empty trash',
    id: '7M1pUf',
  }),
  dialogEmptyTrashNotice: defineMessage({
    description: 'Library empty trash dialog: warning text',
    defaultMessage:
      "By default the media server doesn't remove information for media right away. This helps if a drive is temporarily disconnected. When you empty the trash for a library, all information about missing media is deleted.",
    id: 'kDc7YL',
  }),
}
