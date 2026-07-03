import type { Meta, StoryObj } from '@storybook/vue3-vite'

import CreateEdit from './CreateEdit.vue'
import { mockPage } from '@/mocks/api/pageable'
import { PageRequest } from '@/types/PageRequest'
import { handleGetSharingLabels } from '@/generated/openapi/msw.gen'

const meta = {
  component: CreateEdit,
  render: (args: object) => ({
    components: { CreateEdit },
    setup() {
      return { args }
    },
    template: '<CreateEdit :model-value="args.modelValue" v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
    msw: {
      handlers: [
        handleGetSharingLabels({
          status: 200,
          body: mockPage(['kids', 'teens'], PageRequest.Unpaged()),
        }),
      ],
    },
  },
  args: {},
} satisfies Meta<typeof CreateEdit>

export default meta
type Story = StoryObj<typeof meta>

export const CreateUser: Story = {
  args: {
    modelValue: {
      email: '',
      password: '',
      roles: ['PAGE_STREAMING', 'FILE_DOWNLOAD'],
      sharedLibraries: {
        all: true,
        libraryIds: ['1', '2'],
      },
      ageRestriction: {
        age: 0,
        restriction: 'NONE',
      },
    },
  },
}

export const UpdateUser: Story = {
  args: {
    modelValue: {
      id: '123',
      email: 'user@example.org',
      password: 'masked',
      roles: ['KOBO_SYNC', 'FILE_DOWNLOAD'],
      sharedLibraries: {
        all: false,
        libraryIds: ['1'],
      },
      ageRestriction: {
        age: 12,
        restriction: 'ALLOW_ONLY',
      },
      labelsAllow: ['kids'],
      labelsExclude: ['teens'],
    },
  },
}
