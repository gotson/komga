import type { Meta, StoryObj } from '@storybook/vue3-vite'

import StepOptions from './StepOptions.vue'
import { SeriesCover } from '@/types/SeriesCover'

const meta = {
  component: StepOptions,
  render: (args: object) => ({
    components: { StepOptions },
    setup() {
      return { args }
    },
    template: '<StepOptions :model-value="args.modelValue" v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof StepOptions>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: {
      analyzeDimensions: false,
      convertToCbz: false,
      hashFiles: false,
      hashKoreader: false,
      hashPages: false,
      repairExtensions: false,
      seriesCover: SeriesCover.FIRST,
    },
  },
}
