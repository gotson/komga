import type { Meta, StoryObj } from '@storybook/vue3-vite'

import StepMetadata from './StepMetadata.vue'

const meta = {
  component: StepMetadata,
  render: (args: object) => ({
    components: { StepMetadata },
    setup() {
      return { args }
    },
    template: '<StepMetadata :model-value="args.modelValue" v-bind="args"/>',
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
} satisfies Meta<typeof StepMetadata>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: {
      importBarcodeIsbn: false,
      importComicInfoBook: false,
      importComicInfoCollection: false,
      importComicInfoReadList: false,
      importComicInfoSeries: false,
      importComicInfoSeriesAppendVolume: false,
      importEpubBook: false,
      importEpubSeries: false,
      importLocalArtwork: false,
      importMylarSeries: false,
    },
  },
}
