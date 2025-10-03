import type { Meta, StoryObj } from '@storybook/vue3-vite'

import FileNamePicker from './FileNamePicker.vue'
import { fn } from 'storybook/test'
import { mockBooks } from '@/mocks/api/handlers/books'

const meta = {
  component: FileNamePicker,
  render: (args: object) => ({
    components: { FileNamePicker },
    setup() {
      return { args }
    },
    template: '<FileNamePicker v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {
    dialog: true,
    existingName: 'existing filename.cbz',
    onSelectedName: fn(),
  },
} satisfies Meta<typeof FileNamePicker>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    seriesBooks: mockBooks(5),
  },
}

export const LargeList: Story = {
  args: {
    seriesBooks: mockBooks(1000),
  },
}

export const NoBooks: Story = {
  args: {},
}
