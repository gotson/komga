import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Organize from './Organize.vue'

import { mockReadList1 } from '@/mocks/api/handlers/readlists'
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
