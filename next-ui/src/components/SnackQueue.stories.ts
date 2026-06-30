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
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof SnackQueue>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [{ message: 'Default notification', timer: false }]
  },
}

export const NoTimeout: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        message: 'I will not timeout, click me to make me go away',
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
        message: 'Success',
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
        message: 'Warning',
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
        message: 'Error',
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
        message: 'Info',
        color: 'info',
        timer: false,
      },
    ]
  },
}

export const ActionRouter: Story = {
  args: {},
  play: () => {
    const messagesStore = useMessagesStore()
    messagesStore.messages = [
      {
        message: 'Router',
        action: {
          label: 'open link',
          to: '/route',
        },
        timer: false,
      },
    ]
  },
}
