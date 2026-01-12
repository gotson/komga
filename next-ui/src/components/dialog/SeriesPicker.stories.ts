import type { Meta, StoryObj } from '@storybook/vue3-vite'

import { http, delay } from 'msw'
import SeriesPicker from './SeriesPicker.vue'
import { response401Unauthorized } from '@/mocks/api/handlers'
import { fn } from 'storybook/test'

const meta = {
  component: SeriesPicker,
  render: (args: object) => ({
    components: { SeriesPicker },
    setup() {
      return { args }
    },
    template: '<SeriesPicker v-bind="args"/>',
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
    dialog: true,
    onSelectedSeries: fn(),
  },
} satisfies Meta<typeof SeriesPicker>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const PresetSearch: Story = {
  args: {
    searchString: 'd',
  },
}

export const NoResults: Story = {
  args: {
    searchString: 'not found',
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/series/list', async () => await delay(2_000))],
    },
  },
}

export const Error: Story = {
  args: {
    searchString: 'd',
  },
  parameters: {
    msw: {
      handlers: [http.all('*/api/v1/series/list', response401Unauthorized)],
    },
  },
}
