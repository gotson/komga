import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Organize from './Organize.vue'

import { mockReadList1 } from '@/mocks/api/handlers/readlists'
import { handleGetBooks } from '@/generated/openapi/msw.gen'
import { response200OK } from '@/mocks/api/utils'
import { mockPage } from '@/mocks/api/pageable'
import { mockBooks } from '@/mocks/api/handlers/books'
import { PageRequest } from '@/types/PageRequest'
import { VCard } from 'vuetify/components'

const meta = {
  render: (args: object) => ({
    components: { Organize, VCard },
    inheritAttrs: false,
    setup() {
      return { args }
    },
    template: `
      <v-card title="Organize">
        <template #text>
          <Organize v-bind="args"/>
        </template>
      </v-card>
    `,
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {
    modelValue: {
      ...mockReadList1,
      bookIds: [...Array(15).keys()].map((index) => `BOOK${index + 1}`),
    },
  },
} satisfies Meta<typeof Organize>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

const largeCount = 1000
export const LargeList: Story = {
  args: {
    modelValue: {
      ...mockReadList1,
      bookIds: [...Array(largeCount).keys()].map((index) => `BOOK${index + 1}`),
    },
  },
  beforeEach({ msw }) {
    msw.use(
      handleGetBooks(() => response200OK(mockPage(mockBooks(largeCount), PageRequest.Unpaged()))),
    )
  },
}
