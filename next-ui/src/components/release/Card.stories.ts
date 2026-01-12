import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Card from './Card.vue'

const meta = {
  component: Card,
  render: (args: object) => ({
    components: { Card },
    setup() {
      return { args }
    },
    template: '<Card :release="args.release"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'A card showing the details of a Komga release.',
      },
    },
  },
  args: {},
} satisfies Meta<typeof Card>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    release: {
      version: '1.21.1',
      releaseDate: new Date('2025-03-06T07:31:00Z'),
      url: 'https://github.com/gotson/komga/releases/tag/1.21.1',
      latest: false,
      preRelease: false,
      description:
        "## Changelog\n\n## 🐛 Fixes\n**api**\n- book import would return incorrect matched series ([10e0bde](https://github.com/gotson/komga/commits/10e0bde))\n\n\n## Contributors\nWe'd like to thank the following people for their contributions:\nGauthier Roebroeck",
    },
  },
}

export const Latest: Story = {
  args: {
    ...Default.args,
    latest: true,
  },
}

export const Current: Story = {
  args: {
    ...Default.args,
    current: true,
  },
}

export const LatestAndCurrent: Story = {
  args: {
    ...Default.args,
    latest: true,
    current: true,
  },
}
