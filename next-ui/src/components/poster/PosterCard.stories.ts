import type { Meta, StoryObj } from '@storybook/vue3-vite'

import PosterCard from './PosterCard.vue'
import SnackQueue from '@/components/SnackQueue.vue'
import mockThumbnailBase64 from '@/assets/mock-thumbnail.jpg?inline'
import { base64ToFile } from '@/utils/test-utils'
import type {
  ThumbnailBookDto,
  ThumbnailSeriesCollectionDto,
  ThumbnailSeriesDto,
} from '@/generated/openapi'
import { fn } from 'storybook/test'

const meta = {
  component: PosterCard,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { PosterCard, SnackQueue },
    inheritAttrs: false,
    setup() {
      return { args }
    },
    template: '<PosterCard v-bind="args"/><SnackQueue/>',
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
    onDeleted: fn(),
    onUndeleted: fn(),
    onSelected: fn(),
  },
} satisfies Meta<typeof PosterCard>

export default meta
type Story = StoryObj<typeof meta>

const mockThumbnailFile = base64ToFile(mockThumbnailBase64, 'mock-thumbnail.jpg', 'image/jpeg')

export const LocalFile: Story = {
  args: {
    file: mockThumbnailFile,
    selected: true,
    toDelete: false,
  },
}

export const BookPoster: Story = {
  args: {
    file: {
      bookId: '2',
      id: '12345',
      selected: true,
      type: 'GENERATED',
    } as ThumbnailBookDto,
    selected: true,
    toDelete: false,
  },
}

export const SeriesPoster: Story = {
  args: {
    file: {
      seriesId: '2L',
      fileSize: 1524,
      height: 300,
      width: 250,
      mediaType: 'image/jpeg',
      id: '12345',
      selected: true,
      type: 'SIDECAR',
    } as ThumbnailSeriesDto,
    selected: false,
    toDelete: false,
  },
}

export const CollectionPoster: Story = {
  args: {
    file: {
      collectionId: '1324',
      fileSize: 1524,
      height: 300,
      width: 250,
      mediaType: 'image/jpeg',
      id: '12345',
      selected: true,
      type: 'USER_UPLOADED',
    } as ThumbnailSeriesCollectionDto,
    selected: false,
    toDelete: true,
  },
}
