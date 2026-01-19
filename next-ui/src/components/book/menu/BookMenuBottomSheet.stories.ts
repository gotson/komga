import type { Meta, StoryObj } from '@storybook/vue3-vite'

import BookMenuBottomSheet from './BookMenuBottomSheet.vue'
import { mockSeries1 } from '@/mocks/api/handlers/series'
import { httpTyped } from '@/mocks/api/httpTyped'
import { userRegular } from '@/mocks/api/handlers/users'
import DialogConfirmInstance from '@/components/dialog/ConfirmInstance.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { mockBook } from '@/mocks/api/handlers/books'

const meta = {
  component: BookMenuBottomSheet,
  render: (args: object) => ({
    components: { BookMenuBottomSheet, DialogConfirmInstance, DialogConfirmEditInstance },
    setup() {
      return { args }
    },
    template:
      '<BookMenuBottomSheet v-bind="args" /><DialogConfirmInstance/><DialogConfirmEditInstance/>',
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
    modelValue: true,
    book: mockBook,
  },
} satisfies Meta<typeof BookMenuBottomSheet>

export default meta
type Story = StoryObj<typeof meta>

export const Read: Story = {
  args: {
    book: {
      ...mockBook,
      readProgress: {
        ...mockBook.readProgress,
        completed: true,
      },
    },
  },
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

export const NonAdmin: Story = {
  args: {},
  parameters: {
    msw: {
      handlers: [
        httpTyped.get('/api/v2/users/me', ({ response }) => response(200).json(userRegular)),
      ],
    },
  },
}
