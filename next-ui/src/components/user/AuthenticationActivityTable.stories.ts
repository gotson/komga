import type { Meta, StoryObj } from '@storybook/vue3-vite'

import AuthenticationActivityTable from './AuthenticationActivityTable.vue'
import { delay, http } from 'msw'

import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { handleGetAuthenticationActivity } from '@/generated/openapi/msw.gen'
import { response200OK, response401Unauthorized } from '@/mocks/api/utils'

const meta = {
  component: AuthenticationActivityTable,
  render: (args: object) => ({
    components: { AuthenticationActivityTable },
    setup() {
      return { args }
    },
    template: '<AuthenticationActivityTable v-bind="args" />',
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
} satisfies Meta<typeof AuthenticationActivityTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const ForMe: Story = {
  args: {
    forMe: true,
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [
        http.all('*/api/v2/users/authentication-activity', async () => await delay(5_000)),
      ],
    },
  },
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [
        handleGetAuthenticationActivity(() => response200OK(mockPage([], new PageRequest()))),
      ],
    },
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v2/users/authentication-activity', response401Unauthorized)],
    },
  },
}
