import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Bar from './Bar.vue'
import { useSelectionStore } from '@/stores/selection'

const meta = {
  component: Bar,
  render: (args: object) => ({
    components: { Bar },
    setup() {
      return { args }
    },
    template: '<Bar v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof Bar>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: () => {
    const selectionStore = useSelectionStore()
    selectionStore.selection = ['a', 'b']
  },
}
