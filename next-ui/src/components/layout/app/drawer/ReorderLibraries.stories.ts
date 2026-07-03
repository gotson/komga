import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ReorderLibraries from './ReorderLibraries.vue'

import { ClientSettingUser, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'

import { mockLibraries } from '@/mocks/api/handlers/libraries'
import type { ClientSettingUserUpdateDto, LibraryDto } from '@/generated/openapi'
import { handleGetLibraries, handleGetUserSettings } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

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

const libHandler = handleGetLibraries(() => {
  const bds = {
    ...mockLibraries[0],
    id: '3',
    name: 'BDs',
  } as LibraryDto
  const magazines = {
    ...mockLibraries[0],
    id: '4',
    name: 'Magazines',
  } as LibraryDto
  const manga = {
    ...mockLibraries[0],
    id: '5',
    name: 'Mangas',
  } as LibraryDto
  const libs = [...mockLibraries, bds, magazines, manga]
  return response200OK(libs)
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
        handleGetUserSettings(() => {
          const userLibraries: Record<string, ClientSettingUserLibrary> = {
            '2': {
              unpinned: true,
            },
            '4': {
              unpinned: true,
            },
          }
          const settings: Record<string, ClientSettingUserUpdateDto> = {
            [ClientSettingUser.NextUILibraries]: {
              value: JSON.stringify(userLibraries),
            },
          }
          return response200OK(settings)
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
        handleGetUserSettings(() => {
          const userLibraries: Record<string, ClientSettingUserLibrary> = {
            '1': {
              unpinned: true,
            },
            '2': {
              unpinned: true,
            },
          }
          const settings: Record<string, ClientSettingUserUpdateDto> = {
            [ClientSettingUser.NextUILibraries]: {
              value: JSON.stringify(userLibraries),
            },
          }
          return response200OK(settings)
        }),
      ],
    },
  },
}
