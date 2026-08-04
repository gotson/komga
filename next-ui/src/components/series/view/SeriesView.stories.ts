import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SeriesView from './SeriesView.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'

const meta = {
  component: SeriesView,
  render: (args: object) => ({
    components: { SeriesView },
    setup() {
      return { args }
    },
    template: '<SeriesView />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Standard book view.',
      },
    },
  },
  args: {
    series: mockSeries1,
  },
} satisfies Meta<typeof SeriesView>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const WithBooksData: Story = {
  args: {
    series: {
      ...mockSeries1,
      metadata: {
        ...mockSeries1.metadata,
        totalBookCount: undefined,
        summary: '',
        ageRating: 12,
      },
      booksMetadata: {
        authors: [
          {
            name: 'Tim Marshall',
            role: 'writer',
          },
        ],
        tags: ['bookTag', 'AnotherBookTag'],
        releaseDate: new Date('2014-12-31'),
        summary:
          "All leaders are constrained by geography. Their choices are limited by mountains, rivers, seas and concrete. Yes, to follow world events you need to understand people, ideas and movements - but if you don't know geography, you'll never have the full picture.  If you've ever wondered why Putin is so obsessed with Crimea, why the USA was destined to become a global superpower, or why China's power base continues to expand ever outwards, the answers are all here.  In ten chapters and ten maps, Prisoners of Geography **looks at the past, present and future to offer an essential insight into one of the major factors that determines world history.  It's time to put the 'geo' back into geopolitics.",
        summaryNumber: '1',
        created: new Date('2024-04-14T09:36:55Z'),
        lastModified: new Date('2026-03-27T04:26:36Z'),
      },
    },
  },
}

export const Deleted: Story = {
  args: {
    series: {
      ...mockSeries1,
      deleted: true,
      metadata: {
        ...mockSeries1.metadata,
        totalBookCount: undefined,
        summary: '',
      },
    },
  },
}
