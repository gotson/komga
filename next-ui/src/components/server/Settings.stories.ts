import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Settings from './Settings.vue'
import { fn } from 'storybook/test'

const meta = {
  component: Settings,
  render: (args: object) => ({
    components: { Settings },
    setup() {
      return { args }
    },
    template: '<Settings :settings="args.settings"/>',
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
    settings: {
      deleteEmptyCollections: true,
      deleteEmptyReadLists: false,
      rememberMeDurationDays: 365,
      thumbnailSize: 'XLARGE',
      taskPoolSize: 8,
      serverPort: { configurationSource: 8090, effectiveValue: 8090 },
      serverContextPath: { effectiveValue: '' },
      koboProxy: false,
      kepubifyPath: {
        configurationSource: '/usr/bin/kepubify',
        effectiveValue: '/usr/bin/kepubify',
      },
    },
    onUpdateSettings: fn(),
  },
} satisfies Meta<typeof Settings>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
