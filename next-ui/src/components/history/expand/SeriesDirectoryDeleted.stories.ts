import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SeriesDirectoryDeleted from './SeriesDirectoryDeleted.vue'
import { historySeriesFolderDeleted } from '@/mocks/api/handlers/history'

const meta = {
  component: SeriesDirectoryDeleted,
  render: (args: object) => ({
    components: { SeriesDirectoryDeleted },
    setup() {
      return { args }
    },
    template: '<SeriesDirectoryDeleted v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof SeriesDirectoryDeleted>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    event: historySeriesFolderDeleted,
  },
}
