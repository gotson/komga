import type { Meta, StoryObj } from '@storybook/vue3-vite'

import GenerateDialog from './GenerateDialog.vue'
import { expect, waitFor, within, screen } from 'storybook/test'
import { delay, http, HttpResponse } from 'msw'

const meta = {
  component: GenerateDialog,
  render: (args: object) => ({
    components: { GenerateDialog },
    setup() {
      return { args }
    },
    template: '<GenerateDialog v-model="args.dialog" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {
    dialog: true,
  },
} satisfies Meta<typeof GenerateDialog>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Created: Story = {
  play: async ({ userEvent }) => {
    const canvas = within(screen.getByRole('dialog'))

    await waitFor(() => expect(canvas.getByText(/kobo sync protocol/i)).toBeVisible())
    const comment = canvas.getByLabelText(/comment/i, {
      selector: 'input',
    })
    await userEvent.type(comment, 'new key')

    await userEvent.click(canvas.getByRole('button', { name: /generate/i }))
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/*', async () => await delay(5_000))],
    },
  },
  play: async ({ userEvent }) => {
    const canvas = within(screen.getByRole('dialog'))

    await waitFor(() => expect(canvas.getByText(/kobo sync protocol/i)).toBeVisible())
    const comment = canvas.getByLabelText(/comment/i, {
      selector: 'input',
    })
    await userEvent.type(comment, 'long loading')

    await userEvent.click(canvas.getByRole('button', { name: /generate/i }))
  },
}

export const DuplicateError: Story = {
  parameters: {
    msw: {
      handlers: [
        http.post('*/api/v2/users/me/api-keys', () =>
          HttpResponse.json({ message: 'ERR_1034' }, { status: 400 }),
        ),
      ],
    },
  },
  play: async ({ userEvent }) => {
    const canvas = within(screen.getByRole('dialog'))

    await waitFor(() => expect(canvas.getByText(/kobo sync protocol/i)).toBeVisible())
    const comment = canvas.getByLabelText(/comment/i, {
      selector: 'input',
    })
    await userEvent.type(comment, 'duplicate')

    await userEvent.click(canvas.getByRole('button', { name: /generate/i }))
  },
}
