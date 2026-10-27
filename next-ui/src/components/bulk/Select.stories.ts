import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Select from './Select.vue'
import { ReadingDirectionValues } from '@/types/ReadingDirection'

const readingDirectionOptions = ReadingDirectionValues.map((x) => ({
  title: x.toString(),
  value: x.toString(),
}))

const meta = {
  component: Select,
  render: (args: object) => ({
    components: { Select },
    setup() {
      return { args }
    },
    template: '<Select/>',
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
    allowUnset: true,
    items: readingDirectionOptions,
    modelValue: undefined,
  },
} satisfies Meta<typeof Select>
export default meta

type Story = StoryObj<typeof meta>

export const AllSet: Story = {
  args: {
    initialCounts: [{ value: 'WEBTOON', count: 2 }],
    totalCount: 2,
  },
}

export const Mixed: Story = {
  args: {
    initialCounts: [
      { value: 'LEFT_TO_RIGHT', count: 2 },
      { value: null, count: 3 },
      { value: 'WEBTOON', count: 3 },
    ],
    totalCount: 8,
  },
}

export const AllUnset: Story = {
  args: {
    initialCounts: [{ value: null, count: 2 }],
    totalCount: 2,
  },
}
