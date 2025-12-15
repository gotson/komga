import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Drawer from './Drawer.vue'
import { useAppStore } from '@/stores/app'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import SnackQueue from '@/components/SnackQueue.vue'

const meta = {
  component: Drawer,
  render: (args: object) => ({
    components: { Drawer, DialogConfirmEditInstance, DialogConfirmInstance, SnackQueue },
    setup() {
      return { args }
    },
    template: '<Drawer /><DialogConfirmEditInstance/><DialogConfirmInstance/><SnackQueue/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof Drawer>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: () => {
    const appStore = useAppStore()
    appStore.drawer = true
  },
}
