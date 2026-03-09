import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SharingLabel from './SharingLabel.vue'
import { fn } from 'storybook/test'
import { httpTyped } from '@/mocks/api/httpTyped'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'

const meta = {
  component: SharingLabel,
  render: (args: object) => ({
    components: { SharingLabel },
    setup() {
      return { args }
    },
    template: '<SharingLabel v-model="args.modelValue"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'SharingLabel filter.',
      },
    },
  },
  args: {
    'onUpdate:modelValue': fn(),
    modelValue: [],
  },
} satisfies Meta<typeof SharingLabel>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const NoData: Story = {
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/sharing-labels', ({ response }) =>
          response(200).json(mockPage([], new PageRequest())),
        ),
      ],
    },
  },
}

export const InitialValue: Story = {
  args: {
    modelValue: [
      { i: 'e', v: 'SharingLabel 3' },
      { i: 'i', v: 'SharingLabel 5' },
      { i: 'i', v: 'SharingLabel 8' },
    ],
  },
}

export const InitialValueOutsideShown: Story = {
  args: {
    modelValue: [{ i: 'i', v: 'SharingLabel 100' }],
  },
}
