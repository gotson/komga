import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Card from './Card.vue'
import { expect, fn } from 'storybook/test'

const meta = {
  component: Card,
  render: (args: object) => ({
    components: { Card },
    setup() {
      return { args }
    },
    template: '<Card :item="args.item"/>',
  }),

  args: {
    onMarkRead: fn(),
  },
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'A card showing the details of a Komga announcement.',
      },
    },
  },
} satisfies Meta<typeof Card>

export default meta
type Story = StoryObj<typeof meta>

export const Unread: Story = {
  args: {
    item: {
      id: 'https://komga.org/blog/ebook-drop2',
      url: 'https://komga.org/blog/ebook-drop2',
      title: 'eBook drop 2',
      summary: 'Version 1.9.0 contains the second feature drop for Ebooks support.',
      content_html:
        '<p>Version <a href="https://github.com/gotson/komga/releases/tag/1.9.0" target="_blank" rel="noopener noreferrer">1.9.0</a> contains the second feature drop for Ebooks support.</p>\n<p>It brings nice additions to the initial release, most notably the read progress will be kept when reading and restored, same as with other books.</p>\n<p>The analysis process of EPUB files was also revamped, and some EPUB files that were not analyzed before should be working fine now.</p>\n<p>While the first release forced all the EPUB files through the Epub Reader, this release brings back compatibility with the Divina Reader for pre-paginated EPUB files containing only images. This also restores support for the Pages API, and thus the compatibility with Tachiyomi, or any OPDS-PSE client.</p>\n<p>Head over to the <a href="https://github.com/gotson/komga/releases/tag/1.9.0" target="_blank" rel="noopener noreferrer">Release Notes</a> for more details on all the new features and fixes.</p>',
      date_modified: new Date('2023-12-15T00:00:00Z'),
      author: {
        name: 'gotson',
        url: 'https://github.com/gotson',
      },
      tags: ['upgrade', 'komga'],
      _komga: {
        read: false,
      },
    },
  },
  play: async ({ args, canvas, userEvent }) => {
    await expect(canvas.getByRole('button')).toBeEnabled()

    await userEvent.click(canvas.getByRole('button'))
    await expect(args.onMarkRead).toHaveBeenCalledWith(args.item.id)
  },
}

export const Read: Story = {
  args: {
    item: {
      ...Unread.args?.item,
      _komga: {
        read: true,
      },
    },
  },
  play: async ({ canvas }) => {
    await expect(canvas.getByRole('button')).toBeDisabled()
  },
}
