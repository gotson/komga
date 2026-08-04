import { defineMessages, type MessageDescriptor } from 'vue-intl'
import type { ClientSettingUserOverviewSection } from '@/types/ClientSettingsUser'

export const OverviewSectionValues = [
  'keep_reading',
  'on_deck',
  'recently_released_books',
  'recently_added_books',
  'recently_added_series',
  'recently_updated_series',
  'recently_read_books',
] as const

export const OverviewSectionsDefault: ClientSettingUserOverviewSection[] =
  OverviewSectionValues.map((section) => ({
    section: section,
  }))

export type OverviewSection = (typeof OverviewSectionValues)[number]

export const overviewSectionMessages: Record<OverviewSection, MessageDescriptor> = defineMessages({
  keep_reading: {
    description: 'Overview section: keep reading',
    defaultMessage: 'Keep Reading',
    id: 'enum.OverviewSection.keep_reading',
  },
  on_deck: {
    description: 'Overview section: on deck',
    defaultMessage: 'On Deck',
    id: 'enum.OverviewSection.on_deck',
  },
  recently_released_books: {
    description: 'Overview section: recently released books',
    defaultMessage: 'Recently Released Books',
    id: 'enum.OverviewSection.recently_released_books',
  },
  recently_added_books: {
    description: 'Overview section: recently added books',
    defaultMessage: 'Recently Added Books',
    id: 'enum.OverviewSection.recently_added_books',
  },
  recently_added_series: {
    description: 'Overview section: recently added series',
    defaultMessage: 'Recently Added Series',
    id: 'enum.OverviewSection.recently_added_series',
  },
  recently_updated_series: {
    description: 'Overview section: recently updated series',
    defaultMessage: 'Recently Updated Series',
    id: 'enum.OverviewSection.recently_updated_series',
  },
  recently_read_books: {
    description: 'Overview section: recently read books',
    defaultMessage: 'Recently Read Books',
    id: 'enum.OverviewSection.recently_read_books',
  },
})
