import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Details from './Details.vue'

const meta = {
  component: Details,
  render: (args: object) => ({
    components: { Details },
    setup() {
      return { args }
    },
    template: '<Details :user="args.user" />',
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
    user: {
      id: '0JEDA00AV4Z7G',
      email: 'admin@example.org',
      roles: ['ADMIN', 'FILE_DOWNLOAD', 'KOBO_SYNC', 'KOREADER_SYNC', 'PAGE_STREAMING', 'USER'],
      sharedAllLibraries: true,
      sharedLibrariesIds: [],
      labelsAllow: [],
      labelsExclude: [],
    },
  },
} satisfies Meta<typeof Details>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
}
