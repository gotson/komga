import type { Meta, StoryObj } from '@storybook/vue3-vite'
import { VChip } from 'vuetify/components'
import { getMediaTypeColor, MEDIA_TYPE_COLORS } from './image'

const meta = {
  title: 'Components/MediaTypeChips',
  component: VChip,
  render: () => ({
    components: { VChip },
    setup() {
      const mediaTypes = [...Object.keys(MEDIA_TYPE_COLORS), 'unknown']
      const variants = ['elevated', 'flat', 'tonal', 'outlined', 'text', 'plain']
      return { mediaTypes, variants, getMediaTypeColor }
    },
    template: `
      <div class="py-4" v-for="variant in variants">
        <div class="text-title-small text-uppercase py-2">{{ variant }}</div>
        <div class="d-flex ga-2" >
          <v-chip
            v-for="type in mediaTypes"
            :key="type"
            rounded
            size="small"
            :variant="variant"
            :text="type"
            :color="getMediaTypeColor(type)"
          />
        </div>
      </div>
    `,
  }),
} satisfies Meta<typeof VChip>

export default meta
type Story = StoryObj<typeof meta>

export const AllMediaTypes: Story = {}
