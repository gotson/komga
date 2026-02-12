import { defineMessage, type MessageDescriptor } from 'vue-intl'

export const authorRoles: Record<string, MessageDescriptor> = {
  writer: defineMessage({
    description: 'Author role: writer',
    defaultMessage: 'Writer',
    id: '7hwFJo',
  }),
  penciller: defineMessage({
    description: 'Author role: penciller',
    defaultMessage: 'Penciller',
    id: 'II5EFN',
  }),
  inker: defineMessage({
    description: 'Author role: inker',
    defaultMessage: 'Inker',
    id: 'xeiMMk',
  }),
  colorist: defineMessage({
    description: 'Author role: colorist',
    defaultMessage: 'Colorist',
    id: 'k2JkZX',
  }),
  letterer: defineMessage({
    description: 'Author role: letterer',
    defaultMessage: 'Letterer',
    id: '8NDqor',
  }),
  cover: defineMessage({
    description: 'Author role: cover',
    defaultMessage: 'Cover',
    id: 'crClNV',
  }),
  editor: defineMessage({
    description: 'Author role: editor',
    defaultMessage: 'Editor',
    id: 'VtC7Ce',
  }),
  translator: defineMessage({
    description: 'Author role: translator',
    defaultMessage: 'Translator',
    id: 'FZXkIP',
  }),
} as const
