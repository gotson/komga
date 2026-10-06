import type { Meta, StoryObj } from '@storybook/vue3-vite'

import login from './login.vue'
import { http, delay } from 'msw'

import { expect, waitFor, mocked } from 'storybook/test'
import { useMessagesStore } from '@/stores/messages'
import { response200OK, response401Unauthorized, response502BadGateway } from '@/mocks/api/utils'
import { handleGetGlobalSettings, handleGetOAuth2Providers } from '@/generated/openapi/msw.gen'
import { oauth2ProviderStyles } from '@/types/oauth2'
import { performOAuth2LoginRedirect } from '@/functions/oauth2'

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
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
  beforeEach: () => {
    mocked(performOAuth2LoginRedirect).mockImplementation((registrationId) => {
      console.log(`attempted redirect to ${registrationId}`)
    })
  },
} satisfies Meta<typeof login>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Oauth: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetOAuth2Providers(() => {
        const providers = Object.keys(oauth2ProviderStyles).map((it) => ({
          name: it,
          registrationId: it,
        }))
        return response200OK(providers)
      }),
    )
  },
}

export const OauthHideLogin1Provider: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetGlobalSettings(() =>
        response200OK({
          'komga.nextui.oauth2': {
            allowUnauthorized: true,
            value: JSON.stringify({
              hideLoginFields: true,
            }),
          },
        }),
      ),
      handleGetOAuth2Providers(() => {
        const providers = Object.keys(oauth2ProviderStyles)
          .toSpliced(1)
          .map((it) => ({
            name: it,
            registrationId: it,
          }))
        return response200OK(providers)
      }),
    )
  },
  play: async ({ canvas }) => {
    const login = canvas.queryByLabelText(/email/i, {
      selector: 'input',
    })
    await waitFor(() => expect(login).not.toBeVisible())
  },
}

export const OauthHideLoginNoProvider: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetGlobalSettings(() =>
        response200OK({
          'komga.nextui.oauth2': {
            allowUnauthorized: true,
            value: JSON.stringify({
              hideLoginFields: true,
            }),
          },
        }),
      ),
      handleGetOAuth2Providers(() => response200OK([])),
    )
  },
  play: async ({ canvas }) => {
    const login = canvas.queryByLabelText(/email/i, {
      selector: 'input',
    })
    await waitFor(() => expect(login).toBeVisible())
  },
}

export const OauthAutoLogin1Provider: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetGlobalSettings(() =>
        response200OK({
          'komga.nextui.oauth2': {
            allowUnauthorized: true,
            value: JSON.stringify({
              hideLoginFields: true,
              autoLogin: true,
            }),
          },
        }),
      ),
      handleGetOAuth2Providers(() => {
        const providers = Object.keys(oauth2ProviderStyles)
          .toSpliced(1)
          .map((it) => ({
            name: it,
            registrationId: it,
          }))
        return response200OK(providers)
      }),
    )
  },
  play: async () => {
    await waitFor(() => expect(performOAuth2LoginRedirect).toHaveBeenCalledOnce())
  },
}

export const OauthAutoLogin2Provider: Story = {
  beforeEach({ msw }) {
    msw.use(
      handleGetGlobalSettings(() =>
        response200OK({
          'komga.nextui.oauth2': {
            allowUnauthorized: true,
            value: JSON.stringify({
              hideLoginFields: true,
              autoLogin: true,
            }),
          },
        }),
      ),
      handleGetOAuth2Providers(() => {
        const providers = Object.keys(oauth2ProviderStyles)
          .toSpliced(2)
          .map((it) => ({
            name: it,
            registrationId: it,
          }))
        return response200OK(providers)
      }),
    )
  },
  play: async ({ canvas }) => {
    await canvas.findByText(/continue with google/i)
    await waitFor(() => expect(performOAuth2LoginRedirect).not.toHaveBeenCalled())
  },
}

export const Invalid: Story = {
  beforeEach({ msw }) {
    msw.use(http.get('*/api/v2/users/me', response401Unauthorized))
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
  beforeEach({ msw }) {
    msw.use(http.all('*/api/*', async () => await delay(5_000)))
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
  beforeEach({ msw }) {
    msw.use(http.get('*/api/v2/users/me', response502BadGateway))
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
