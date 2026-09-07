import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Links from './Links.vue'
import { mockBook } from '@/mocks/api/handlers/books'
import { pick } from '@/functions/pick'

const meta = {
  component: Links,
  render: (args: object) => ({
    components: { Links },
    setup() {
      return { args }
    },
    template: '<Links v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: { modelValue: pick(mockBook.metadata, 'links', 'linksLock') },
} satisfies Meta<typeof Links>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
