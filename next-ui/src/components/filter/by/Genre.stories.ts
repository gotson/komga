import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Genre from './Genre.vue'
import { fn } from 'storybook/test'
import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

const meta = {
  component: Genre,
  render: (args: object) => ({
    components: { Genre },
    setup() {
      return { args }
    },
    template: '<Genre v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Genre filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof Genre>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/genres', ({ response }) =>
          response(200).json(mockPage([], new PageRequest())),
        ),
      ],
    },
  },
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'e', v: 'Genre 3' },
      { i: 'i', v: 'Genre 5' },
      { i: 'i', v: 'Genre 8' },
    ],
  },
}

export const InitialValueOutsideShown: Story = {
  args: {
    modelValue: [{ i: 'i', v: 'Genre 100' }],
  },
}
