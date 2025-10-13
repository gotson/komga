import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Table from './Table.vue'
import { users } from '@/mocks/api/handlers/users'
import { fn } from 'storybook/test'

const meta = {
  component: Table,
  render: (args: object) => ({
    components: { Table },
    setup() {
      return { args }
    },
    template: '<Table v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {
    onAddUser: fn(),
    onEnterAddUser: fn(),
    onEnterEditUser: fn(),
    onEnterDeleteUser: fn(),
    onEnterChangePassword: fn(),
    onChangePassword: fn(),
    onEditUser: fn(),
    onDeleteUser: fn(),
  },
} satisfies Meta<typeof Table>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    users: users,
  },
}

export const Loading: Story = {
  args: {
    loading: true,
  },
}

export const NoData: Story = {
  args: {
    users: [],
  },
}
