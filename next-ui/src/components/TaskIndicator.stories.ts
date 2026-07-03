import type { Meta, StoryObj } from '@storybook/vue3-vite'

import TaskIndicator from './TaskIndicator.vue'
import { expect } from 'storybook/test'
import { useTaskQueueStore } from '@/stores/task-queue'

const meta = {
  component: TaskIndicator,
  render: (args: object) => ({
    components: { TaskIndicator },
    setup() {
      return { args }
    },
    template: '<TaskIndicator />',
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
} satisfies Meta<typeof TaskIndicator>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Active: Story = {
  args: {},
  play: () => {
    const taskStore = useTaskQueueStore()
    taskStore.count = 1
  },
}

export const Clicked: Story = {
  args: {},
  play: async ({ canvas, userEvent }) => {
    const taskStore = useTaskQueueStore()
    taskStore.count = 1
    taskStore.countByType = {
      AnalyzeBook: 2,
      GenerateBookThumbnail: 1,
      RefreshBookMetadata: 1,
      RefreshSeriesMetadata: 22,
    }

    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
  },
}
