import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReorderLibraries from './ReorderLibraries.vue'
import { httpTyped } from '@/mocks/api/httpTyped'
import { CLIENT_SETTING_USER, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'
import type { components } from '@/generated/openapi/komga'
import { libraries } from '@/mocks/api/handlers/libraries'

const meta = {
  component: ReorderLibraries,
  render: (args: object) => ({
    components: { ReorderLibraries },
    setup() {
      return { args }
    },
    template: '<ReorderLibraries />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof ReorderLibraries>

export default meta
type Story = StoryObj<typeof meta>

const libHandler = httpTyped.get('/api/v1/libraries', ({ response }) => {
  const bds = {
    ...libraries[0],
    id: '3',
    name: 'BDs',
  } as components['schemas']['LibraryDto']
  const magazines = {
    ...libraries[0],
    id: '4',
    name: 'Magazines',
  } as components['schemas']['LibraryDto']
  const manga = {
    ...libraries[0],
    id: '5',
    name: 'Mangas',
  } as components['schemas']['LibraryDto']
  const libs = [...libraries, bds, magazines, manga]
  return response(200).json(libs)
})

export const AllPinned: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [libHandler],
    },
  },
}

export const SomeUnpinned: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [
        libHandler,
        httpTyped.get('/api/v1/client-settings/user/list', ({ response }) => {
          const userLibraries: Record<string, ClientSettingUserLibrary> = {
            '2': {
              unpinned: true,
            },
            '4': {
              unpinned: true,
            },
          }
          const settings: Record<string, components['schemas']['ClientSettingUserUpdateDto']> = {
            [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: {
              value: JSON.stringify(userLibraries),
            },
          }
          return response(200).json(settings)
        }),
      ],
    },
  },
}

export const AllUnpinned: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/client-settings/user/list', ({ response }) => {
          const userLibraries: Record<string, ClientSettingUserLibrary> = {
            '1': {
              unpinned: true,
            },
            '2': {
              unpinned: true,
            },
          }
          const settings: Record<string, components['schemas']['ClientSettingUserUpdateDto']> = {
            [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: {
              value: JSON.stringify(userLibraries),
            },
          }
          return response(200).json(settings)
        }),
      ],
    },
  },
}
