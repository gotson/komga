import type { Meta, StoryObj } from '@storybook/vue3-vite'

import SimpleDataTable from './SimpleDataTable.vue'

const meta = {
  component: SimpleDataTable,
  render: (args: object) => ({
    components: { SimpleDataTable },
    setup() {
      return { args }
    },
    template: '<SimpleDataTable v-bind="args" />',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {
    rows: [
      {
        header: 'Header Text',
        data: 'Value 1',
      },
      {
        header: 'Links',
        data: [
          { text: 'Komga', href: 'https://komga.org' },
          { text: 'Google', href: 'https://www.google.com' },
        ],
      },
      {
        header: 'Long value',
        data: '/data/Books/Comics/Super Long Path/Again/more/again/this is getting crazy long/wait for it/The Lucky Devils/The Lucky Devils 007 (2026) (Digital) (Mephisto-Empire).cbz',
      },
      {
        header: 'Header Chip',
        data: [
          { text: 'Value 1' },
          { text: 'Value 2' },
          { text: 'Value 3' },
          { text: 'Value 4' },
          { text: 'Value 5' },
          { text: 'Value 6' },
          { text: 'Value 7' },
          { text: 'Value 8' },
          { text: 'Value 9' },
          { text: 'Value 10' },
          { text: 'Value 11' },
          { text: 'Value 12' },
          { text: 'Value 13' },
          { text: 'Value 14' },
          { text: 'Value 15' },
          { text: 'Value 16' },
          { text: 'Value 17' },
          { text: 'Value 18' },
          { text: 'Value 19' },
          { text: 'Value 20' },
          { text: 'Value 21' },
          { text: 'Value 22' },
          { text: 'Value 23' },
          { text: 'Value 24' },
          { text: 'Value 25' },
          { text: 'Value 26' },
          { text: 'Value 27' },
          { text: 'Value 28' },
          { text: 'Value 29' },
          { text: 'Value 30' },
        ],
      },
      {
        header: 'Long value no space',
        data: '/data/Books/Comics/SuperLongPath/Again/more/again/thisisgettingcrazylong/waitforit/TheLuckyDevils/TheLuckyDevils007(2026)(Digital)(Mephisto-Empire).cbz',
      },
    ],
  },
} satisfies Meta<typeof SimpleDataTable>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
