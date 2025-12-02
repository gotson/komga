import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Libraries from './Libraries.vue'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import { expect, waitFor } from 'storybook/test'
import { CLIENT_SETTING_USER, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'
import type { components } from '@/generated/openapi/komga'
import { VList } from 'vuetify/components'

const meta = {
  component: Libraries,
  render: (args: object) => ({
    components: { Libraries, VList },
    setup() {
      return { args }
    },
    template: '<v-list nav><Libraries /></v-list>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
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

export const NonAdmin: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userRegular)),
      ],
    },
  },
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByLabelText(/add library/i)).toBeNull())
    await waitFor(() => expect(canvas.queryByLabelText(/libraries menu/i)).not.toBeNull())
    await waitFor(() => expect(canvas.queryAllByLabelText(/library menu/i)).toHaveLength(0))
  },
}

export const Unpinned: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/client-settings/user/list', ({ response }) => {
          const userLibraries: Record<string, ClientSettingUserLibrary> = {
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
  play: async ({ canvas, userEvent }) => {
    await waitFor(() => userEvent.click(canvas.getByText(/more/i)))
    await waitFor(() => expect(canvas.queryByText(/comics/i)).toBeVisible())
  },
}

export const Ordered: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v1/client-settings/user/list', ({ response }) => {
          const userLibraries: Record<string, ClientSettingUserLibrary> = {
            '1': {
              order: 2,
            },
            '2': {
              order: 1,
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
