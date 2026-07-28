import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Libraries from './Libraries.vue'

import { userRegular } from '@/mocks/api/handlers/users'
import { expect, waitFor } from 'storybook/test'
import { ClientSettingUser, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'

import { VList } from 'vuetify/components'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import SnackQueue from '@/components/SnackQueue.vue'
import { delay, http } from 'msw'
import { mockLibraries } from '@/mocks/api/handlers/libraries'
import type { ClientSettingUserUpdateDto } from '@/generated/openapi'
import {
  handleGetCurrentUser,
  handleGetLibraries,
  handleGetUserSettings,
} from '@/generated/openapi/msw.gen'
import { response200OK, response401Unauthorized } from '@/mocks/api/utils'

const meta = {
  component: Libraries,
  render: (args: object) => ({
    components: { Libraries, VList, DialogConfirmEditInstance, DialogConfirmInstance, SnackQueue },
    setup() {
      return { args }
    },
    template:
      '<v-list nav><Libraries /></v-list><DialogConfirmEditInstance/><DialogConfirmInstance/><SnackQueue/>',
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
} satisfies Meta<typeof Libraries>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByLabelText(/add library/i)).not.toBeNull())
    await waitFor(() => expect(canvas.queryByLabelText(/libraries menu/i)).not.toBeNull())
    await waitFor(() => expect(canvas.queryAllByLabelText(/library menu/i)).not.toBeNull())
  },
}

export const Unavailable: Story = {
  args: {},

  beforeEach({ msw }) {
    msw.use(
      handleGetLibraries({
        status: 200,
        body: mockLibraries.map((it) => ({ ...it, unavailable: true })),
      }),
      handleGetUserSettings(() => {
        const userLibraries: Record<string, ClientSettingUserLibrary> = {
          '2': {
            unpinned: true,
          },
        }
        const settings: Partial<Record<ClientSettingUser, ClientSettingUserUpdateDto>> = {
          [ClientSettingUser.NextUILibraries]: {
            value: JSON.stringify(userLibraries),
          },
        }
        return response200OK(settings)
      }),
    )
  },

  play: async ({ canvas, userEvent }) => {
    await waitFor(() => userEvent.click(canvas.getByText(/more/i)))
    await waitFor(() => expect(canvas.queryByText(/comics/i)).toBeVisible())
    await waitFor(() => expect(canvas.queryAllByText(/unavailable/i)).toHaveLength(2))
  },
}

export const NonAdmin: Story = {
  beforeEach({ msw }) {
    msw.use(handleGetCurrentUser(() => response200OK(userRegular)))
  },

  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByLabelText(/add library/i)).toBeNull())
    await waitFor(() => expect(canvas.queryByLabelText(/libraries menu/i)).not.toBeNull())
    await waitFor(() => expect(canvas.queryAllByLabelText(/library menu/i)).toHaveLength(0))
  },
}

export const Unpinned: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetUserSettings(() => {
        const userLibraries: Record<string, ClientSettingUserLibrary> = {
          '2': {
            unpinned: true,
          },
        }
        const settings: Partial<Record<ClientSettingUser, ClientSettingUserUpdateDto>> = {
          [ClientSettingUser.NextUILibraries]: {
            value: JSON.stringify(userLibraries),
          },
        }
        return response200OK(settings)
      }),
    )
  },

  play: async ({ canvas, userEvent }) => {
    await waitFor(() => userEvent.click(canvas.getByText(/more/i)))
    await waitFor(() => expect(canvas.queryByText(/comics/i)).toBeVisible())
  },
}

export const Ordered: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetUserSettings(() => {
        const userLibraries: Record<string, ClientSettingUserLibrary> = {
          '1': {
            order: 2,
          },
          '2': {
            order: 1,
          },
        }
        const settings: Partial<Record<ClientSettingUser, ClientSettingUserUpdateDto>> = {
          [ClientSettingUser.NextUILibraries]: {
            value: JSON.stringify(userLibraries),
          },
        }
        return response200OK(settings)
      }),
    )
  },
}

export const Loading: Story = {
  beforeEach({ msw }) {
    msw.use(http.all('*/api/*', async () => await delay(5_000)))
  },
}

export const CreationError: Story = {
  beforeEach({ msw }) {
    msw.use(handleGetLibraries(response401Unauthorized))
  },
}
