import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ChangePassword from './ChangePassword.vue'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: ChangePassword,
  render: (args: object) => ({
    components: { ChangePassword },
    setup() {
      return { args }
    },
    template: '<ChangePassword />',
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
} satisfies Meta<typeof ChangePassword>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const MatchingPasswords: Story = {
  args: {},
  play: async ({ canvas, userEvent }) => {
    const password1 = canvas.getByLabelText(/new password/i, {
      selector: 'input',
    })
    await userEvent.type(password1, 'abc')

    const password2 = canvas.getByLabelText(/confirm password/i, {
      selector: 'input',
    })
    await userEvent.type(password2, 'abc')

    await waitFor(() => expect(canvas.queryByText(/must be identical/i)).toBeNull())
  },
}

export const DifferentPasswords: Story = {
  args: {},
  play: async ({ canvas, userEvent }) => {
    const password1 = canvas.getByLabelText(/new password/i, {
      selector: 'input',
    })
    await userEvent.type(password1, 'abc')

    const password2 = canvas.getByLabelText(/confirm password/i, {
      selector: 'input',
    })
    await userEvent.type(password2, 'def')

    await waitFor(() => expect(canvas.getByText(/must be identical/i)).toBeVisible())
  },
}
