import type { Meta, StoryObj } from '@storybook/vue3-vite'

import DuplicatePageDeleted from './DuplicatePageDeleted.vue'
import { historyDuplicatePageDeleted } from '@/mocks/api/handlers/history'

const meta = {
  component: DuplicatePageDeleted,
  render: (args: object) => ({
    components: { DuplicatePageDeleted },
    setup() {
      return { args }
    },
    template: '<DuplicatePageDeleted v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof DuplicatePageDeleted>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    event: historyDuplicatePageDeleted,
  },
}
