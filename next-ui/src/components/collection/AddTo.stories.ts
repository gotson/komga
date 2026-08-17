import type { Meta, StoryObj } from '@storybook/vue3-vite'

import AddTo from './AddTo.vue'
import { expect, waitFor } from 'storybook/test'
import { handleGetCollections } from '@/generated/openapi/msw.gen'
import { response200OK } from '@/mocks/api/utils'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { mockCollections } from '@/mocks/api/handlers/collections'

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
        component: 'Collection selection with ability to create one',
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

    await waitFor(() => expect(canvas.queryByText(/golden age/i)).toBeVisible())
    await waitFor(() => expect(canvas.queryByText(/iron age/i)).toBeVisible())
  },
}

export const Filtered: Story = {
  play: async ({ canvas, userEvent }) => {
    const search = canvas.getByRole('textbox')
    await userEvent.type(search, 'iron')

    const createButton = canvas.getByRole('button', { name: /^create$/i })
    await waitFor(() => expect(createButton).toBeEnabled())

    await waitFor(() => expect(canvas.queryByText(/golden age/i)).toBeNull())
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
    await userEvent.type(search, 'golden age')

    const createButton = canvas.getByRole('button', { name: /^create$/i })
    await waitFor(() => expect(createButton).toBeDisabled())

    await waitFor(() => expect(canvas.queryByText(/already exists/i)).toBeVisible())
  },
}

export const LongList: Story = {
  beforeEach({ msw }) {
    const collections = mockCollections(500)
    msw.use(
      handleGetCollections(({ request }) => {
        const query = new URL(request.url).searchParams
        const search = query.get('search')

        const selected = collections.filter((it) => {
          let include = true
          if (search) include = include && !!it.name.match(new RegExp(search, 'i'))
          return include
        })

        return response200OK(
          mockPage(selected, new PageRequest(Number(query.get('page')), Number(query.get('size')))),
        )
      }),
    )
  },
}
