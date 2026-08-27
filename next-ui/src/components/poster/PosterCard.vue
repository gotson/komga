<template>
  <v-card :width="width">
    <div>
      <v-img
        :src="imageUrl"
        aspect-ratio="0.7071"
        class="poster-card"
        :class="{ 'cursor-pointer': canSelect, 'is-selected': selected, 'is-todelete': toDelete }"
        @click="canSelect ? emit('selected', file) : undefined"
      >
        <v-avatar
          v-if="selected"
          color="primary"
          class="position-absolute top-0 right-0 elevation-2 ma-2"
        >
          <v-icon
            icon="i-mdi:check"
            color="white"
          />
        </v-avatar>

        <v-avatar
          v-if="toDelete"
          color="error"
          class="position-absolute top-0 right-0 elevation-2 ma-2"
        >
          <v-icon
            icon="i-mdi:trash"
            color="white"
          />
        </v-avatar>
      </v-img>
    </div>

    <v-card-text>
      <div class="d-flex flex-column ga-1">
        <div
          v-if="type"
          class="d-flex align-center ga-2"
        >
          <v-icon :icon="type.icon" />
          <span>{{ type.text }}</span>
        </div>
        <div>
          {{
            fileSize ??
            $formatMessage({
              description: 'Poster card: unknown file size',
              defaultMessage: 'Unknown file size',
              id: 'MYYgTv',
            })
          }}
        </div>
        <div>
          {{
            fileDimensions
              ? $formatMessage(
                  {
                    description: 'Poster card: file dimensions',
                    defaultMessage: 'w: {width}, h: {height}',
                    id: 'IlRX+s',
                  },
                  {
                    width: fileDimensions.width,
                    height: fileDimensions.height,
                  },
                )
              : $formatMessage({
                  description: 'Poster card: unknown file dimensions',
                  defaultMessage: 'Unknown dimensions',
                  id: 'TkIvo8',
                })
          }}
        </div>
        <div>
          {{
            fileMediaType ??
            $formatMessage({
              description: 'Poster card: unknown media type',
              defaultMessage: 'Unknown media type',
              id: 'd5c33V',
            })
          }}
        </div>
      </div>
    </v-card-text>

    <v-card-actions>
      <v-spacer />
      <v-btn
        :disabled="!canDelete"
        :text="
          toDelete
            ? $formatMessage({
                description: 'Poster card: action - undo',
                defaultMessage: 'Undo',
                id: 'u23YLt',
              })
            : $formatMessage({
                description: 'Poster card: action - delete',
                defaultMessage: 'Delete',
                id: 'aHyrmS',
              })
        "
        @click="toDelete ? emit('undeleted', file) : emit('deleted', file)"
      />
    </v-card-actions>
  </v-card>
</template>

<script setup lang="ts">
import { type PosterDto, resolvePosterUrl } from '@/functions/poster'
import { useIntl } from 'vue-intl'
import { getFileSize } from '@/utils/utils'
import { watchImmediate } from '@vueuse/core'
import type { ImageDimensions } from '@/types/image'
import { pick } from '@/functions/pick'

const intl = useIntl()

const {
  file,
  width = 220,
  selected,
  toDelete,
} = defineProps<{
  file: File | PosterDto
  width?: number | 'auto'
  selected: boolean
  toDelete: boolean
}>()

const emit = defineEmits<{
  selected: [poster: File | PosterDto]
  deleted: [poster: File | PosterDto]
  undeleted: [poster: File | PosterDto]
}>()

// store the objectUrl for local file
const localFileObjectUrl = ref<string | undefined>()
const localFileDimensions = ref<ImageDimensions | undefined>()

watchImmediate(
  () => file,
  (newFile) => {
    // always revoke the previous URL to free browser memory
    if (localFileObjectUrl.value) {
      URL.revokeObjectURL(localFileObjectUrl.value)
      localFileObjectUrl.value = undefined
    }

    if (newFile instanceof File) {
      localFileObjectUrl.value = URL.createObjectURL(newFile)

      // calculate dimensions
      const img = new Image()
      img.onload = () => {
        localFileDimensions.value = {
          width: img.naturalWidth,
          height: img.naturalHeight,
        }
      }
      img.src = localFileObjectUrl.value
    }
  },
)

onUnmounted(() => {
  if (localFileObjectUrl.value) URL.revokeObjectURL(localFileObjectUrl.value)
})

const imageUrl = computed(() => {
  if (file instanceof File) return localFileObjectUrl.value
  return resolvePosterUrl(file)
})

const fileSize = computed(() => {
  if (file instanceof File) return getFileSize(file.size)
  return getFileSize(file.fileSize)
})

const fileMediaType = computed(() => {
  if (file instanceof File) return file.type
  return file.mediaType
})

const fileDimensions = computed(() => {
  if (file instanceof File) return localFileDimensions.value
  if (file.width && file.height) return pick(file, 'width', 'height')
  return undefined
})

const type = computed(() => {
  if (file instanceof File)
    return {
      icon: 'i-mdi:cloud-upload',
      text: intl.formatMessage({
        description: 'Poster card: status - local file',
        defaultMessage: 'Local file',
        id: 'jnjpZy',
      }),
    }
  switch (file.type) {
    case 'SIDECAR':
      return {
        icon: 'i-mdi:folder',
        text: intl.formatMessage({
          description: 'Poster card: type - local artwork',
          defaultMessage: 'Local artwork',
          id: 'USJuJ1',
        }),
      }
    case 'GENERATED':
      return {
        icon: 'i-mdi:file',
        text: intl.formatMessage({
          description: 'Poster card: type - generated artwork',
          defaultMessage: 'Generated artwork',
          id: 'vtBARB',
        }),
      }
    case 'USER_UPLOADED':
      return {
        icon: 'i-mdi:cloud-check',
        text: intl.formatMessage({
          description: 'Poster card: type - user uploaded',
          defaultMessage: 'User uploaded',
          id: '9dNd68',
        }),
      }
  }
})

const canDelete = computed(() => file instanceof File || file.type === 'USER_UPLOADED')
const canSelect = computed(() => !selected && !toDelete)
</script>

<style scoped lang="scss">
.poster-card {
  position: relative;
  overflow: hidden;
  outline: 4px solid transparent;
  outline-offset: -4px;

  &.is-selected {
    outline-color: rgb(var(--v-theme-primary));
  }

  &.is-todelete {
    outline-color: rgb(var(--v-theme-error));
  }
}

.selection-ribbon {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 45px 45px 0;
  border-color: transparent rgb(var(--v-theme-primary)) transparent transparent;
  z-index: 2;
}

.selection-ribbon .v-icon {
  position: absolute;
  top: 5px;
  right: -40px;
}
</style>
