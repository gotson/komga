import type { Meta, StoryObj } from '@storybook/vue3-vite'

import TabNavigation from './TabNavigation.vue'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: TabNavigation,
  render: (args: object) => ({
    components: { TabNavigation },
    setup() {
      return { args }
    },
    template: '<TabNavigation v-bind="args"/>',
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
} satisfies Meta<typeof TabNavigation>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    routes: [
      { title: 'Recommended', icon: 'i-mdi:star', to: '' },
      { title: 'Series', icon: 'i-mdi:bookshelf', to: '' },
      { title: 'Books', icon: 'i-mdi:book-multiple', to: '' },
      { title: 'Collections', icon: 'i-mdi:layers-triple', to: '' },
      { title: 'Read Lists', icon: 'i-mdi:bookmark-multiple', to: '' },
    ],
  },
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByText(/collections/i)).not.toBeNull())
    await waitFor(() => expect(canvas.queryByText(/read lists/i)).not.toBeNull())
  },
}

export const NoCollection: Story = {
  args: {
    routes: [
      { title: 'Recommended', icon: 'i-mdi:star', to: '' },
      { title: 'Series', icon: 'i-mdi:bookshelf', to: '' },
      { title: 'Books', icon: 'i-mdi:book-multiple', to: '' },
      { title: 'Read Lists', icon: 'i-mdi:bookmark-multiple', to: '' },
    ],
  },
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByText(/collections/i)).toBeNull())
    await waitFor(() => expect(canvas.queryByText(/read lists/i)).not.toBeNull())
  },
}

export const NoReadList: Story = {
  args: {
    routes: [
      { title: 'Recommended', icon: 'i-mdi:star', to: '' },
      { title: 'Series', icon: 'i-mdi:bookshelf', to: '' },
      { title: 'Books', icon: 'i-mdi:book-multiple', to: '' },
      { title: 'Collections', icon: 'i-mdi:layers-triple', to: '' },
    ],
  },
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByText(/collections/i)).not.toBeNull())
    await waitFor(() => expect(canvas.queryByText(/read lists/i)).toBeNull())
  },
}

export const NoCollectionNorReadList: Story = {
  args: {
    routes: [
      { title: 'Recommended', icon: 'i-mdi:star', to: '' },
      { title: 'Series', icon: 'i-mdi:bookshelf', to: '' },
      { title: 'Books', icon: 'i-mdi:book-multiple', to: '' },
    ],
  },
  play: async ({ canvas }) => {
    await waitFor(() => expect(canvas.queryByText(/collections/i)).toBeNull())
    await waitFor(() => expect(canvas.queryByText(/read lists/i)).toBeNull())
  },
}
