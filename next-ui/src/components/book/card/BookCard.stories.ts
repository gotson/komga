import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookCard from './BookCard.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { fn } from 'storybook/test'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import { mockBook } from '@/mocks/api/handlers/books'

const meta = {
  component: BookCard,
  render: (args: object) => ({
    components: { BookCard, DialogConfirmEditInstance, DialogConfirmInstance },
    setup() {
      return { args }
    },
    template: '<BookCard v-bind="args" /><DialogConfirmEditInstance/><DialogConfirmInstance/>',
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
    book: mockBook,
    onSelection: fn(),
  },
} satisfies Meta<typeof BookCard>

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

export const Selected: Story = {
  args: {
    selected: true,
  },
}

export const Hover: Story = {
  args: {},
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}

export const HoverNonAdmin: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userRegular)),
      ],
    },
  },
  play: ({ canvas, userEvent }) => {
    userEvent.hover(canvas.getByRole('img'))
  },
}
