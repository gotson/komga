import type { Meta, StoryObj } from '@storybook/vue3-vite'

import FormattedMessage from './FormattedMessage'

const meta = {
  component: FormattedMessage,
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Component to display formatted `formatjs` messages using dynamic slots.',
      },
    },
  },
  args: {},
} satisfies Meta<typeof FormattedMessage>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    messageDescriptor: {
      defaultMessage: 'Please accept <a>terms <b>and<br></br></b> conditions</a> first',
      id: 'test',
    },
  },
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage v-bind="args">
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
export const Values: Story = {
  args: {
    messageDescriptor: {
      defaultMessage:
        'Please accept <a>terms <b>and<br></br></b> conditions</a> first, <b>{name}</b>',
      id: 'test',
    },
    values: {
      name: 'John',
    },
  },
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage v-bind="args">
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
  args: {
    messageDescriptor: {
      defaultMessage: 'Please accept terms and conditions first',
      id: 'test',
    },
  },
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage v-bind="args"/>
    `,
  }),
}

export const NoMarkupTag: Story = {
  args: {
    messageDescriptor: {
      defaultMessage: 'Please accept terms and conditions first',
      id: 'test',
    },
    tag: 'div',
  },
  render: (args: object) => ({
    components: { FormattedMessage },
    setup() {
      return { args }
    },
    template: `
      <FormattedMessage v-bind="args"/>
    `,
  }),
}
