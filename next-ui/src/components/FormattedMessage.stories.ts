import type { Meta, StoryObj } from '@storybook/vue3-vite'

import FormattedMessage from './FormattedMessage.ts'

const meta = {
  component: FormattedMessage,
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof Details>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage :messageDescriptor="{
        defaultMessage: 'Please accept <a>terms <b>and<br></br></b> conditions</a> first',
        id: 'test',
      }">
        <template #a="Content">
          <a href="#" target="_blank">
            <component :is="Content"/>
          </a>
        </template>
        <template #b="Content">
          <strong>
            <component :is="Content"/>
          </strong>
        </template>
        <template #br>
          <br/>
        </template>
      </FormattedMessage>
    `,
  }),
}

export const NoMarkup: Story = {
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage :messageDescriptor="{
        defaultMessage: 'Please accept terms and conditions first',
        id: 'test',
      }"/>
    `,
  }),
}

export const NoMarkupTag: Story = {
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage :messageDescriptor="{
        defaultMessage: 'Please accept terms and conditions first',
        id: 'test',
      }" tag="div"/>
    `,
  }),
}
