import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SearchList from './SearchList.vue'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: SearchList,
  render: (args: object) => ({
    components: { SearchList },
    setup() {
      return { args }
    },
    template: '<SearchList />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Searchable filter list.',
      },
    },
  },
  args: {
    items: [
      { title: 'Tag 1', value: '+tag1', valueExclude: '-tag1' },
      { title: 'Tag 2', value: '+tag2', valueExclude: '-tag2' },
      { title: 'Tag 3', value: '+tag3', valueExclude: '-tag3' },
      { title: 'Tag include only', value: '+tag4' },
    ],
    searchItems: [{ title: 'Tag 1 (search result)', value: '+tag1', valueExclude: '-tag1' }],
  },
} satisfies Meta<typeof SearchList>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const InitialValue: Story = {
  args: {
    modelValue: ['+tag1', '-tag2', 'crap'],
  },
}

export const Search: Story = {
  args: {
    search: 't',
  },
  play: async ({ canvas, userEvent }) => {
    const search = canvas.getByLabelText(/search/i, {
      selector: 'input',
    })
    await userEvent.type(search, 'tag')

    await waitFor(() => expect(canvas.getByText(/result/i)).toBeVisible())
  },
}
