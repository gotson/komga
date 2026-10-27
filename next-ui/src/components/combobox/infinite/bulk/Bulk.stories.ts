import type { Meta, StoryObj } from '@storybook/vue3-vite'

import InfiniteBulk from './Bulk.vue'
import { expect, waitFor } from 'storybook/test'

const meta = {
  component: InfiniteBulk,
  render: (args: object) => ({
    components: { InfiniteBulk },
    setup() {
      return { args }
    },
    template: '<InfiniteBulk v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: `
A variation of the Infinite combobox that can manage a list of values for multiple underlying items.
Each tag can be in 1 of 4 states, transitions are defined as follows:

| Initial State                    | Click Chip/List Action (Cycle)                                                                                               | Click Close (X) Action                                                                            |
|:---------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------|
| **\`mixed\`** *(partial)*          | Transitions to **\`new\`**                                                                                                | Transitions directly to **\`remove\`**                                                              |
| **\`all\`** *(100% present)*       | Transitions to **\`remove\`**                                                                                             | Transitions directly to **\`remove\`**                                                              |
| **\`new\`** *(added in session)*   | **Pre-existing item**: Transitions to **\`remove\`**. **Draft item**: Removed from model completely                       | **Pre-existing item**: Transitions to **\`remove\`**. **Draft item**: Removed from model completely |
| **\`remove\`** *(marked deletion)* | **If \`initialCount == totalCount\`**: Restores to **\`all\`**. **If \`initialCount < totalCount\`**: Restores to **\`mixed\`** | Restores to original state (**\`all\`** or **\`mixed\`**)                                             |
        `,
      },
    },
  },
  args: {
    modelValue: [],
    items: ['Gaming', 'Programming', 'Vue', 'Vuetify'],
    label: 'text-input',
    initialCounts: [],
    totalCount: 30,
  },
} satisfies Meta<typeof InfiniteBulk>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}

export const Existing: Story = {
  args: {
    initialCounts: [
      { value: 'Gaming', count: 30 },
      { value: 'Manga', count: 20 },
      { value: 'Anime', count: 2 },
    ],
    totalCount: 30,
  },
}

export const ClickOutside: Story = {
  play: async ({ canvas, userEvent }) => {
    const combobox = canvas.getByLabelText('text-input')
    await userEvent.type(combobox, 'example')

    await expect(combobox).toHaveValue('example')

    // click outside
    await userEvent.click(document.body)

    await waitFor(() => expect(combobox).toHaveValue(''))
  },
}

export const PressEsc: Story = {
  play: async ({ canvas, userEvent }) => {
    const combobox = canvas.getByLabelText('text-input')
    await userEvent.type(combobox, 'example')

    await expect(combobox).toHaveValue('example')

    // press escape key
    await userEvent.keyboard('{Escape}')

    await waitFor(() => expect(combobox).toHaveValue(''))
  },
}
