import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ContainerChip from './ContainerChip.vue'
import { collectionPosterUrl } from '@/api/images'

const meta = {
  component: ContainerChip,
  render: (args: object) => ({
    components: { ContainerChip },
    inheritAttrs: false,
    setup() {
      return { args }
    },
    template: '<ContainerChip v-bind="args" /><br/><br/><ContainerChip v-bind="args" small />',
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
} satisfies Meta<typeof ContainerChip>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    containers: [
      {
        text: 'Collection 1',
        imageUrl: collectionPosterUrl('1'),
        link: { name: '/' },
      },
    ],
  },
}

export const Multiple: Story = {
  args: {
    containers: [
      {
        text: 'Collection 1  but with a super long name that nammdememaeme',
        imageUrl: collectionPosterUrl('1'),
        link: { name: '/' },
      },
      {
        text: 'Collection 2',
        imageUrl: collectionPosterUrl('2'),
      },
      {
        text: 'Collection 3',
        imageUrl: collectionPosterUrl('3'),
      },
      {
        text: 'Collection no poster',
      },
    ],
  },
}
