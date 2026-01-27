import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ItemCardWide from './ItemCardWide.vue'
import { seriesThumbnailUrl } from '@/api/images'
import { delay, http } from 'msw'
import { fn } from 'storybook/test'

const meta = {
  component: ItemCardWide,
  render: (args: object) => ({
    components: { ItemCardWide },
    setup() {
      return { args }
    },
    template: '<ItemCardWide v-bind="args" />',
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
    title: 'Card title',
    text: 'Card content',
    posterUrl: seriesThumbnailUrl('id'),
    width: 150,
    onSelection: fn(),
    onClickFab: fn(),
    onClickQuickAction: fn(),
    onClickMenu: fn(),
    preSelect: false,
    selected: false,
    stretchPoster: true,
    menuIcon: 'i-mdi:menu',
    quickActionIcon: 'i-mdi:pencil',
  },
  argTypes: {
    posterUrl: {
      options: [seriesThumbnailUrl('id'), seriesThumbnailUrl('idL')],
      control: { type: 'radio' },
    },
  },
} satisfies Meta<typeof ItemCardWide>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    stretchPoster: true,
  },
}

export const LongText: Story = {
  args: {
    title:
      'The Fractured World of Miravara this is a super long title that should overshoot the container, but that should still be alright because it should wrap nicely',
    text: `The story unfolds on Miravara, a planet encased within a colossal, translucent barrier known as the Glass Frontier. For centuries, this barrier has protected the populace from a dying sun, filtering its radiation into usable power. Entire cities cling to its surface like glowing barnacles—each one a hybrid of sleek crystal towers and decaying steel foundations. Below the barrier lies the Underglow, a vast labyrinth of machinery and forgotten people who maintain the world’s vital energy grid, unseen by those above.

Politically, Miravara is divided between the High Dominion—wealthy citizens who live near the surface of the barrier—and the Conduits, a laboring caste living in the Underglow. Above them all looms the enigmatic Council of Luminarchs, a secretive group rumored to communicate directly with the Glass Frontier itself.`,
    stretchPoster: true,
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

export const LandscapeStretched: Story = {
  args: {
    topRightIcon: 'i-mdi:check',
    posterUrl: seriesThumbnailUrl('idL'),
    stretchPoster: true,
  },
}

export const LandscapeNotStretched: Story = {
  args: {
    topRightIcon: 'i-mdi:check',
    posterUrl: seriesThumbnailUrl('idL'),
    stretchPoster: false,
  },
}

export const QuickActionIcon: Story = {
  args: {
    quickActionIcon: 'i-mdi:pencil',
  },
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const MenuIcon: Story = {
  args: {
    menuIcon: 'i-mdi:menu',
  },
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const SelectableHover: Story = {
  args: {},
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const DisableSelection: Story = {
  args: {
    disableSelection: true,
  },
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

export const Progress: Story = {
  args: {
    progressPercent: 33,
  },
}

export const Big: Story = {
  args: {
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
