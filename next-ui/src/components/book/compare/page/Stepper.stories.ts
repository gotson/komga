import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Stepper from './Stepper.vue'

const meta = {
  component: Stepper,
  render: (args: object) => ({
    components: { Stepper },
    setup() {
      return { args }
    },
    template: '<Stepper />',
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
    modelValue: 1,
    max: 50,
  },
} satisfies Meta<typeof Stepper>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    controlProps: {
      variant: 'elevated',
      elevation: 5,
      color: 'primary',
    },
  },
}

export const HideCount: Story = {
  args: {
    hideCount: true,
    controlProps: {
      variant: 'elevated',
      elevation: 5,
      color: 'primary',
    },
  },
}

export const HideWhenIdle: Story = {
  args: {
    hideWhenIdle: true,
    controlProps: {
      variant: 'tonal',
      color: 'primary',
    },
  },
}

export const HideCountAndWhenIdle: Story = {
  args: {
    hideCount: true,
    hideWhenIdle: true,
    controlProps: {
      variant: 'tonal',
      color: 'primary',
    },
  },
}

export const Pill: Story = {
  args: {
    pill: true,
    controlProps: {
      variant: 'flat',
      color: 'surface',
    },
  },
}

export const PillHideCount: Story = {
  args: {
    pill: true,
    hideCount: true,
    controlProps: {
      variant: 'flat',
      color: 'surface',
    },
  },
}

export const PillHideWhenIdle: Story = {
  args: {
    pill: true,
    hideWhenIdle: true,
    controlProps: {
      variant: 'flat',
      color: 'surface',
    },
  },
}

export const PillHideCountAndWhenIdle: Story = {
  args: {
    pill: true,
    hideCount: true,
    hideWhenIdle: true,
    controlProps: {
      variant: 'flat',
      color: 'surface',
    },
  },
}
