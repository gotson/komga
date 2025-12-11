import type { Meta, StoryObj } from '@storybook/vue3-vite'

import StepScanner from './StepScanner.vue'
import { ScanInterval } from '@/types/ScanInterval'

const meta = {
  component: StepScanner,
  render: (args: object) => ({
    components: { StepScanner },
    setup() {
      return { args }
    },
    template: '<StepScanner :model-value="args.modelValue" v-bind="args"/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
  },
  args: {},
} satisfies Meta<typeof StepScanner>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {
    modelValue: {
      emptyTrashAfterScan: false,
      oneshotsDirectory: '_oneshots',
      scanCbx: true,
      scanDirectoryExclusions: ['#recycle', '@eaDir', '@Recycle'],
      scanEpub: true,
      scanForceModifiedTime: false,
      scanInterval: ScanInterval.DAILY,
      scanOnStartup: false,
      scanPdf: true,
    },
  },
}
