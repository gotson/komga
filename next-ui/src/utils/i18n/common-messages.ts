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
  filterPanelHeader: defineMessage({
    description: 'Filter panel header',
    defaultMessage: 'FILTERS',
    id: '0sIkhg',
  }),
  filterPanelCreators: defineMessage({
    description: 'Filter panel sub-section: Creators',
    defaultMessage: 'Creators',
    id: 'WT63aY',
  }),
  filterPanelSort: defineMessage({
    description: 'Filter panel sub-section: Sort',
    defaultMessage: 'SORT',
    id: 'I9L5Ag',
  }),
  filterPanelReadStatus: defineMessage({
    description: 'Filter panel: Read status',
    defaultMessage: 'Read status',
    id: 'XRbUyd',
  }),
  filterPanelSeriesStatus: defineMessage({
    description: 'Filter panel: Series status',
    defaultMessage: 'Status',
    id: 'Pp3+1S',
  }),
  filterPanelLibrary: defineMessage({
    description: 'Filter panel: Library',
    defaultMessage: 'Library',
    id: 'phzvRy',
  }),
  filterPanelGenre: defineMessage({
    description: 'Filter panel: Genre',
    defaultMessage: 'Genre',
    id: 'hBanlz',
  }),
  filterPanelTag: defineMessage({
    description: 'Filter panel: Tag',
    defaultMessage: 'Tag',
    id: 'NwK2Kv',
  }),
  filterPanelPublisher: defineMessage({
    description: 'Filter panel: Publisher',
    defaultMessage: 'Publisher',
    id: 'NruW40',
  }),
  filterPanelReleaseYear: defineMessage({
    description: 'Filter panel: Release year',
    defaultMessage: 'Release year',
    id: 'xpBvz8',
  }),
  filterPanelAgeRating: defineMessage({
    description: 'Filter panel: Age rating',
    defaultMessage: 'Age rating',
    id: '8P7Yn+',
  }),
  filterPanelLanguage: defineMessage({
    description: 'Filter panel: Language',
    defaultMessage: 'Language',
    id: '3qQjnw',
  }),
  filterPanelSharingLabel: defineMessage({
    description: 'Filter panel: Sharing label',
    defaultMessage: 'Sharing label',
    id: 'hGFogR',
  }),
}
