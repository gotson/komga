import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ItemCard from './ItemCard.vue'
import { seriesThumbnailUrl } from '@/api/images'
import { delay, http } from 'msw'
import { fn } from 'storybook/test'

const meta = {
  component: ItemCard,
  render: (args: object) => ({
    components: { ItemCard },
    setup() {
      return { args }
    },
    template: '<ItemCard v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {
    title: 'Card title',
    posterUrl: seriesThumbnailUrl('id'),
    width: 150,
    onSelection: fn(),
  },
} satisfies Meta<typeof ItemCard>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    line1: 'Line 1',
    line2: 'Line 2',
    stretchPoster: true,
  },
}

export const LongText: Story = {
  args: {
    title: 'A very long title that will wrap',
    line1: 'A very long title that will wrap',
    line2: 'A very long title that will wrap',
    stretchPoster: true,
  },
}

export const EmptyLines: Story = {
  args: {
    allowEmptyLine1: true,
    allowEmptyLine2: true,
  },
}

export const NoEmptyLines: Story = {
  args: {
    allowEmptyLine1: false,
    allowEmptyLine2: false,
  },
}

export const TopRightCount: Story = {
  args: {
    topRight: 24,
  },
}

export const TopRightIcon: Story = {
  args: {
    topRightIcon: 'i-mdi:check',
  },
}

export const SelectableHover: Story = {
  args: {},
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const Selected: Story = {
  args: {
    selected: true,
  },
}

export const PreSelect: Story = {
  args: {
    preSelect: true,
  },
}

export const Big: Story = {
  args: {
    line1: 'Line 1',
    line2: 'Line 2',
    width: 300,
  },
}

export const PosterError: Story = {
  args: {
    posterUrl: '/error',
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/*', async () => await delay(5_000))],
    },
  },
}
