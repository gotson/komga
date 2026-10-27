import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Edit from './Edit.vue'
import { mockSeries1, mockSeries2 } from '@/mocks/api/handlers/series'

const meta = {
  component: Edit,
  render: (args: object) => ({
    components: { Edit },
    setup() {
      return { args }
    },
    template: '<Edit v-bind="args"/>',
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
      entities: [{ entity: mockSeries1 }, { entity: mockSeries2 }],
      bulkUpdates: {
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
        genres: [],
        genresLock: undefined,
        tags: [],
        tagsLock: undefined,
        sharingLabels: [],
        sharingLabelsLock: undefined,
      },
      uploadQueue: [],
    },
  },
} satisfies Meta<typeof Edit>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
