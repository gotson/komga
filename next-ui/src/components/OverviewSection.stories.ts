import type { Meta, StoryObj } from '@storybook/vue3-vite'

import OverviewSection from './OverviewSection.vue'

const meta = {
  component: OverviewSection,
  render: (args: object) => ({
    components: { OverviewSection },
    setup() {
      return { args }
    },
    template: '<OverviewSection />',
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
    libraryViewId: 'all',
    section: { section: 'recently_added_books' },
  },
} satisfies Meta<typeof OverviewSection>

export default meta
type Story = StoryObj<typeof meta>

export const RecentlyAddedBooks: Story = {}

export const RecentlyAddedSeries: Story = {
  args: {
    section: { section: 'recently_added_series' },
  },
}
