import type { Meta, StoryObj } from '@storybook/vue3-vite'

import CreateEdit from './CreateEdit.vue'
import { ScanInterval } from '@/types/ScanInterval'
import { SeriesCover } from '@/types/SeriesCover'
import { getLibraryDefaults } from '@/modules/libraries'

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
  },
  args: {},
} satisfies Meta<typeof CreateEdit>

export default meta
type Story = StoryObj<typeof meta>

export const Create: Story = {
  args: {
    createMode: true,
    modelValue: getLibraryDefaults(),
  },
}

export const Edit: Story = {
  args: {
    createMode: false,
    modelValue: {
      analyzeDimensions: false,
      convertToCbz: false,
      emptyTrashAfterScan: false,
      hashFiles: false,
      hashKoreader: false,
      hashPages: false,
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
      name: 'Existing',
      oneshotsDirectory: '_oneshots',
      repairExtensions: false,
      root: '/comics',
      scanCbx: true,
      scanDirectoryExclusions: [],
      scanEpub: true,
      scanForceModifiedTime: false,
      scanInterval: ScanInterval.DAILY,
      scanOnStartup: false,
      scanPdf: true,
      seriesCover: SeriesCover.FIRST,
    },
  },
}
