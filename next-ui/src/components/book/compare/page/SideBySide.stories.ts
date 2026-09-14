import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SideBySide from './SideBySide.vue'
import { transientBookPageUrl } from '@/api/images'

const meta = {
  component: SideBySide,
  render: (args: object) => ({
    components: { SideBySide },
    setup() {
      return { args }
    },
    template: '<SideBySide />',
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
    left: {
      number: 1,
      fileName: 'P00001.jpg',
      mediaType: 'image/jpeg',
      width: 1000,
      height: 1530,
      sizeBytes: 34060,
      size: '33.3 KiB',
      url: transientBookPageUrl('1', 1),
    },
    right: {
      number: 1,
      fileName: 'P00001.jpg',
      mediaType: 'image/jpeg',
      width: 1000,
      height: 1530,
      sizeBytes: 34060,
      size: '33.3 KiB',
      url: transientBookPageUrl('1', 1),
    },
  },
} satisfies Meta<typeof SideBySide>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
