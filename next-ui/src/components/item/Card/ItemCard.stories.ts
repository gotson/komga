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
    docs: {
      description: {
        component: 'A flexible card that serves as the base for entity cards.',
      },
    },
  },
  args: {
    title: { text: 'Card title' },
    posterUrl: seriesThumbnailUrl('id'),
    width: 150,
    onSelection: fn(),
    onClickFab: fn(),
    preSelect: false,
    selected: false,
  },
} satisfies Meta<typeof ItemCard>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    lines: [{ text: 'Line 1' }, { text: 'Line 2' }],
    stretchPoster: true,
  },
}

export const LongText: Story = {
  args: {
    title: {
      text: 'Short 2 lines',
      lines: 2,
    },
    lines: [
      {
        text: 'A very long title that will wrap but that is very long so it takes more lines',
        lines: 2,
      },
      { text: 'Short 2 lines', lines: 2 },
    ],
    stretchPoster: true,
  },
}

export const EmptyLines: Story = {
  args: {
    lines: [{ allowEmpty: true }, { allowEmpty: true }],
  },
}

export const NoEmptyLines: Story = {
  args: {
    lines: [{ allowEmpty: false }, { allowEmpty: false }],
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

export const FabHover: Story = {
  args: {
    fabIcon: 'i-mdi:check',
    disableSelection: true,
  },
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const Selected: Story = {
  args: {
    selected: true,
    fabIcon: 'i-mdi:check',
  },
}

export const PreSelect: Story = {
  args: {
    preSelect: true,
  },
}

export const Big: Story = {
  args: {
    lines: [{ text: 'Line 1' }, { text: 'Line 2' }],
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
