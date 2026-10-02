import { defineMessage, type MessageDescriptor } from 'vue-intl'

export const NOTABLE_ENTITY_ANYKIND = 'anykind' as const

export const DefaultNotableEntitiesKinds = ['character', 'team', 'location'] as const

export const notableEntitiesKindsMessages: Record<string, MessageDescriptor> = {
  [NOTABLE_ENTITY_ANYKIND]: defineMessage({
    description: 'Notable entity filter: any kind',
    defaultMessage: 'All notable entities',
    id: 'KTifVk',
  }),
  character: defineMessage({
    description: 'Notable entity kind: character',
    defaultMessage: 'Character',
    id: '1hsKmu',
  }),
  team: defineMessage({
    description: 'Notable entity kind: team',
    defaultMessage: 'Team',
    id: '5Xt1ke',
  }),
  location: defineMessage({
    description: 'Notable entity kind: location',
    defaultMessage: 'Location',
    id: 'bRAV2F',
  }),
} as const
