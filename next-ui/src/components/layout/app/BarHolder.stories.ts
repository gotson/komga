import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BarHolder from './BarHolder.vue'
import { useSelectionStore } from '@/stores/selection'

const meta = {
  component: BarHolder,
  render: (args: object) => ({
    components: { BarHolder },
    setup() {
      const selectionStore = useSelectionStore()
      return { args, selectionStore }
    },
    template:
      '<BarHolder v-bind="args"/><v-btn @click="selectionStore.selection.push(`a`)">Add item</v-btn>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component:
          'Holder to display the app bar, or the selection bar on top when items are selected.',
      },
    },
  },
  args: {},
} satisfies Meta<typeof BarHolder>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: () => {
    const selectionStore = useSelectionStore()
    selectionStore.selection = ['a', 'b']
  },
}
