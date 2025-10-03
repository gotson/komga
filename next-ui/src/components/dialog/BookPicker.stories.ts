import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookPicker from './BookPicker.vue'
import { fn } from 'storybook/test'
import { mockBooks } from '@/mocks/api/handlers/books'

const meta = {
  component: BookPicker,
  render: (args: object) => ({
    components: { BookPicker },
    setup() {
      return { args }
    },
    template: '<BookPicker v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {
    dialog: true,
    onSelectedBook: fn(),
    books: mockBooks(5),
  },
} satisfies Meta<typeof BookPicker>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    ...meta.args,
  },
}
export const PresetFilter: Story = {
  args: {
    filter: '3',
    ...meta.args,
  },
}

export const LargeList: Story = {
  args: {
    ...meta.args,
    books: mockBooks(500),
  },
}
