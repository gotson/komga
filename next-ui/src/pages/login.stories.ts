import type { Meta, StoryObj } from '@storybook/vue3-vite'

import login from './login.vue'
import { http, delay } from 'msw'

import { response401Unauthorized, response502BadGateway } from '@/mocks/api/handlers'
import { expect, waitFor } from 'storybook/test'
import { useMessagesStore } from '@/stores/messages'

const meta = {
  component: login,
  render: (args: object) => ({
    components: { login },
    setup() {
      return { args }
    },
    template: '<login />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof login>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Invalid: Story = {
  parameters: {
    msw: {
      handlers: [http.get('*/api/v2/users/me', response401Unauthorized)],
    },
  },
  play: async ({ canvas, userEvent }) => {
    const login = canvas.getByLabelText(/email/i, {
      selector: 'input',
    })
    await userEvent.type(login, 'test@example.org')

    const password = canvas.getByLabelText(/password/i, {
      selector: 'input',
    })
    await userEvent.type(password, 'abc')

    await userEvent.click(canvas.getByRole('button', { name: /sign in/i }))

    await waitFor(() => expect(canvas.getByText(/invalid login/i)).toBeVisible())
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [http.all('*/api/*', async () => await delay(5_000))],
    },
  },
  play: async ({ canvas, userEvent }) => {
    const login = canvas.getByLabelText(/email/i, {
      selector: 'input',
    })
    await userEvent.type(login, 'test@example.org')

    const password = canvas.getByLabelText(/password/i, {
      selector: 'input',
    })
    await userEvent.type(password, 'abc')

    await userEvent.click(canvas.getByRole('button', { name: /sign in/i }))
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [http.post('*/api/*', response502BadGateway)],
    },
  },
  play: async ({ canvas, userEvent }) => {
    const login = canvas.getByLabelText(/email/i, {
      selector: 'input',
    })
    await userEvent.type(login, 'test@example.org')

    const password = canvas.getByLabelText(/password/i, {
      selector: 'input',
    })
    await userEvent.type(password, 'abc')

    await userEvent.click(canvas.getByRole('button', { name: /sign in/i }))

    const messagesStore = useMessagesStore()
    await waitFor(() => expect(messagesStore.messages.length).toBe(1))
  },
}
