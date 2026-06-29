import type { Meta, StoryObj } from '@storybook/vue3-vite'

import claim from './claim.vue'
import { http, delay } from 'msw'

import { expect, waitFor } from 'storybook/test'
import { useMessagesStore } from '@/stores/messages'
import { handleGetClaimStatus } from '@/generated/openapi/msw.gen'
import { response200OK, response502BadGateway } from '@/mocks/api/utils'

const meta = {
  component: claim,
  render: (args: object) => ({
    components: { claim },
    setup() {
      return { args }
    },
    template: '<claim />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    msw: {
      handlers: [handleGetClaimStatus(() => response200OK({ isClaimed: false }))],
    },
  },
  args: {},
} satisfies Meta<typeof claim>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Invalid: Story = {
  play: async ({ canvas, userEvent }) => {
    const login = canvas.getByLabelText(/email/i, {
      selector: 'input',
    })
    await userEvent.type(login, 'test@example.org')

    const password1 = canvas.getByLabelText(/^password/i, {
      selector: 'input',
    })
    await userEvent.type(password1, 'abc')

    const password2 = canvas.getByLabelText(/confirm password/i, {
      selector: 'input',
    })
    await userEvent.type(password2, 'def')

    await userEvent.click(canvas.getByRole('button', { name: /create/i }))
  },
}

export const Loading: Story = {
  parameters: {
    msw: {
      handlers: [
        ...meta.parameters.msw.handlers,
        http.post('*/api/*', async () => await delay(5_000)),
      ],
    },
  },
  play: async ({ canvas, userEvent }) => {
    const login = canvas.getByLabelText(/email/i, {
      selector: 'input',
    })
    await userEvent.type(login, 'test@example.org')

    const password1 = canvas.getByLabelText(/^password/i, {
      selector: 'input',
    })
    await userEvent.type(password1, 'abc')

    const password2 = canvas.getByLabelText(/confirm password/i, {
      selector: 'input',
    })
    await userEvent.type(password2, 'abc')

    await userEvent.click(canvas.getByRole('button', { name: /create/i }))
  },
}

export const Error: Story = {
  parameters: {
    msw: {
      handlers: [...meta.parameters.msw.handlers, http.post('*/api/*', response502BadGateway)],
    },
  },
  play: async ({ canvas, userEvent }) => {
    const login = canvas.getByLabelText(/email/i, {
      selector: 'input',
    })
    await userEvent.type(login, 'test@example.org')

    const password1 = canvas.getByLabelText(/^password/i, {
      selector: 'input',
    })
    await userEvent.type(password1, 'abc')

    const password2 = canvas.getByLabelText(/confirm password/i, {
      selector: 'input',
    })
    await userEvent.type(password2, 'abc')

    await userEvent.click(canvas.getByRole('button', { name: /create/i }))

    const messagesStore = useMessagesStore()
    await waitFor(() => expect(messagesStore.messages.length).toBe(1))
  },
}
