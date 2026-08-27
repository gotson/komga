import type { Meta, StoryObj } from '@storybook/vue3-vite'

import PosterUpload from './PosterUpload.vue'
import SnackQueue from '@/components/SnackQueue.vue'
import { expect, fireEvent, waitFor } from 'storybook/test'
import { base64ToFile } from '@/utils/test-utils'
import mockThumbnailBase64 from '@/assets/mock-thumbnail.jpg?inline'
import type { ThumbnailBookDto } from '@/generated/openapi'

const meta = {
  component: PosterUpload,
  subcomponents: { SnackQueue },
  render: (args: object) => ({
    components: { PosterUpload, SnackQueue },
    inheritAttrs: false,
    setup() {
      return { args }
    },
    template: '<PosterUpload v-bind="args"/><SnackQueue/>',
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
    entityPosters: [],
  },
} satisfies Meta<typeof PosterUpload>

export default meta
type Story = StoryObj<typeof meta>

const mockThumbnailFile = base64ToFile(mockThumbnailBase64, 'mock-thumbnail.jpg', 'image/jpeg')

export const Default: Story = {
  args: {
    entityPosters: [
      {
        bookId: '2',
        id: '1',
        fileSize: 1524,
        height: 300,
        width: 250,
        mediaType: 'image/jpeg',
        selected: false,
        type: 'GENERATED',
      } as ThumbnailBookDto,
      {
        bookId: '2',
        id: '2',
        fileSize: 1385,
        height: 350,
        width: 200,
        mediaType: 'image/webp',
        selected: false,
        type: 'SIDECAR',
      } as ThumbnailBookDto,
      {
        bookId: '2',
        id: '3',
        fileSize: 900,
        height: 400,
        width: 350,
        mediaType: 'image/avif',
        selected: true,
        type: 'USER_UPLOADED',
      } as ThumbnailBookDto,
    ],
  },
}

export const LocalFiles: Story = {
  args: {
    entityPosters: [
      {
        bookId: '2',
        id: '1',
        fileSize: 1524,
        height: 300,
        width: 250,
        mediaType: 'image/jpeg',
        selected: true,
        type: 'GENERATED',
      } as ThumbnailBookDto,
      {
        bookId: '2',
        id: '2',
        fileSize: 1385,
        height: 350,
        width: 200,
        mediaType: 'image/webp',
        selected: false,
        type: 'SIDECAR',
      } as ThumbnailBookDto,
      {
        bookId: '2',
        id: '3',
        fileSize: 900,
        height: 400,
        width: 350,
        mediaType: 'image/avif',
        selected: false,
        type: 'USER_UPLOADED',
      } as ThumbnailBookDto,
    ],
  },
  play: async ({ canvasElement, canvas }) => {
    const fileInput = canvasElement.querySelector('input[type="file"]')
    if (!fileInput) {
      throw new Error('Could not find an input element with type="file"')
    }
    await fireEvent.change(fileInput, {
      target: { files: [mockThumbnailFile] },
    })
    await waitFor(() => expect(canvas.getByText(/local artwork/i)).toBeVisible())
  },
}
