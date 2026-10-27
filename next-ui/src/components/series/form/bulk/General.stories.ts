import type { Meta, StoryObj } from '@storybook/vue3-vite'

import General from './General.vue'
import { mockSeries1, mockSeries2 } from '@/mocks/api/handlers/series'

const meta = {
  component: General,
  render: (args: object) => ({
    components: { General },
    setup() {
      return { args }
    },
    template: '<General v-bind="args"/>',
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
    modelValue: {
      status: undefined,
      statusLock: undefined,
      language: undefined,
      languageLock: undefined,
      readingDirection: undefined,
      readingDirectionLock: undefined,
      publisher: undefined,
      publisherLock: undefined,
      ageRating: undefined,
      ageRatingLock: undefined,
    },
    entities: [mockSeries1.metadata, mockSeries2.metadata],
    onlyOneShots: false,
  },
} satisfies Meta<typeof General>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const OnlyOneShots: Story = {
  args: {
    onlyOneShots: true,
  },
}
