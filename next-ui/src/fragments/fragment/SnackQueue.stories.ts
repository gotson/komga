import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SnackQueue from './SnackQueue.vue'
import { useMessagesStore } from '@/stores/messages'

const meta = {
  component: SnackQueue,
  render: (args: object) => ({
    components: { SnackQueue },
    setup() {
      return { args }
    },
    template: '<SnackQueue />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof SnackQueue>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [{ text: 'Default notification', timer: false }]
  },
}

export const NoTimeout: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        text: 'I will not timeout, click me to make me go away',
        timeout: -1,
        timer: false,
      },
    ]
  },
}

export const Success: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        text: 'Success',
        color: 'success',
        timer: false,
      },
    ]
  },
}

export const Warning: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        text: 'Warning',
        color: 'warning',
        timer: false,
      },
    ]
  },
}

export const Error: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        text: 'Error',
        color: 'error',
        timer: false,
      },
    ]
  },
}

export const Info: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        text: 'Info',
        color: 'info',
        timer: false,
      },
    ]
  },
}
