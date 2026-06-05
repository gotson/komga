import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookView from './BookView.vue'
import { mockBook } from '@/mocks/api/handlers/books'

const meta = {
  component: BookView,
  render: (args: object) => ({
    components: { BookView },
    setup() {
      return { args }
    },
    template: '<BookView />',
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
    book: mockBook,
  },
} satisfies Meta<typeof BookView>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Unread: Story = {
  args: {
    book: {
      ...mockBook,
      readProgress: undefined,
    },
  },
}

export const InProgress: Story = {
  args: {
    book: {
      ...mockBook,
      readProgress: {
        ...mockBook.readProgress,
        completed: false,
        page: 25,
      },
    },
  },
}

export const Oneshot: Story = {
  args: {
    book: {
      ...mockBook,
      oneshot: true,
    },
  },
}

export const Deleted: Story = {
  args: {
    book: {
      ...mockBook,
      deleted: true,
    },
  },
}

export const MediaOutdated: Story = {
  args: {
    book: {
      ...mockBook,
      media: {
        ...mockBook.media,
        status: 'OUTDATED',
      },
    },
  },
}

export const MediaError: Story = {
  args: {
    book: {
      ...mockBook,
      media: {
        ...mockBook.media,
        status: 'ERROR',
      },
    },
  },
}

export const MediaUnsupported: Story = {
  args: {
    book: {
      ...mockBook,
      media: {
        ...mockBook.media,
        status: 'UNSUPPORTED',
      },
    },
  },
}

export const MediaUnknown: Story = {
  args: {
    book: {
      ...mockBook,
      media: {
        ...mockBook.media,
        status: 'UNKNOWN',
      },
    },
  },
}
