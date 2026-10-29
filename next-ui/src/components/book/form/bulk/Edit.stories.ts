import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Edit from './Edit.vue'
import { mockBook } from '@/mocks/api/handlers/books'

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
      entities: [{ entity: mockBook }, { entity: mockBook }],
      bulkUpdates: {
        authors: [],
        authorsLock: undefined,
        tags: [],
        tagsLock: undefined,
        notableEntities: [],
        notableEntitiesLock: undefined,
        genres: [],
        genresLock: undefined,
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
export const SomeOneshot: Story = {
  args: {
    modelValue: {
      ...meta.args.modelValue,
      entities: [{ entity: mockBook }, { entity: { ...mockBook, oneshot: true } }],
    },
  },
}
