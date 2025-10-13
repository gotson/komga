import type { Meta, StoryObj } from '@storybook/vue3-vite'

import DirectorySelection from './DirectorySelection.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { useAppStore } from '@/stores/app'
import { fn } from 'storybook/test'

const meta = {
  component: DirectorySelection,
  render: (args: object) => ({
    components: { DirectorySelection, DialogConfirmEdit: DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template: '<DirectorySelection v-bind="args"/><DialogConfirmEdit/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {
    onScan: fn(),
  },
} satisfies Meta<typeof DirectorySelection>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const PresetPath: Story = {
  play: () => {
    const appStore = useAppStore()
    appStore.importBooksPath = '/comics'
  },
}
