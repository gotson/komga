import type { Meta, StoryObj } from '@storybook/vue3-vite'

import AddTo from './AddTo.vue'
import { expect, waitFor } from 'storybook/test'
import { handleGetReadLists } from '@/generated/openapi/msw.gen'
import { response200OK } from '@/mocks/api/utils'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { mockReadLists } from '@/mocks/api/handlers/readlists'

const meta = {
  component: AddTo,
  render: (args: object) => ({
    components: { AddTo },
    setup() {
      return { args }
    },
    template: '<div class="d-flex border" style="height: 500px"><AddTo /></div>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Read list selection with ability to create one',
      },
    },
  },
  args: {},
} satisfies Meta<typeof AddTo>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  play: async ({ canvas }) => {
    const createButton = canvas.getByRole('button', { name: /^create$/i })
    await waitFor(() => expect(createButton).toBeDisabled())

    await waitFor(() => expect(canvas.queryByText(/readlist example/i)).toBeVisible())
    await waitFor(() => expect(canvas.queryByText(/elfes/i)).toBeVisible())
  },
}

export const Filtered: Story = {
  play: async ({ canvas, userEvent }) => {
    const search = canvas.getByRole('textbox')
    await userEvent.type(search, 'elf')

    const createButton = canvas.getByRole('button', { name: /^create$/i })
    await waitFor(() => expect(createButton).toBeEnabled())

    await waitFor(() => expect(canvas.queryByText(/readlist example/i)).toBeNull())
  },
}

export const NoResults: Story = {
  play: async ({ canvas, userEvent }) => {
    const search = canvas.getByRole('textbox')
    await userEvent.type(search, 'whatever')

    const createButton = canvas.getByRole('button', { name: /^create$/i })
    await waitFor(() => expect(createButton).toBeEnabled())

    await waitFor(() => expect(canvas.queryByText(/no results/i)).toBeVisible())
  },
}

export const Duplicate: Story = {
  play: async ({ canvas, userEvent }) => {
    const search = canvas.getByRole('textbox')
    await userEvent.type(search, 'elfes')

    const createButton = canvas.getByRole('button', { name: /^create$/i })
    await waitFor(() => expect(createButton).toBeDisabled())

    await waitFor(() => expect(canvas.queryByText(/already exists/i)).toBeVisible())
  },
}

export const LongList: Story = {
  beforeEach({ msw }) {
    const readlists = mockReadLists(500)
    msw.use(
      handleGetReadLists(({ request }) => {
        const query = new URL(request.url).searchParams
        const selectedReadLists = query.get('search')
          ? readlists.filter(
              (it) => !!it.name.match(new RegExp(query.get('search') as string, 'i')),
            )
          : readlists

        return response200OK(
          mockPage(
            selectedReadLists,
            new PageRequest(
              Number(query.get('page')),
              Number(query.get('size')),
              undefined,
              Boolean(query.get('unpaged')),
            ),
          ),
        )
      }),
    )
  },
}
