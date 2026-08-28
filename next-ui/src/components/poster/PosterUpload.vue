<template>
  <div>
    <v-file-upload
      v-model="uploadQueue"
      multiple
      filter-by-type="image/*"
      @rejected="handleReject()"
      @update:model-value="updateLocalFiles"
    >
      <template #default>
        <v-file-upload-dropzone>
          <template #title>
            <div>
              {{
                $formatMessage({
                  description: 'Poster upload: dropzone title',
                  defaultMessage: 'Drag and drop images here',
                  id: 'sNSGMQ',
                })
              }}
            </div>
            <div class="text-title-medium d-flex ga-2 justify-center">
              <span>{{
                $formatMessage({
                  description:
                    'Poster upload: dropzone subtitle, will be followed by the keyboard hotkey for pasting',
                  defaultMessage: 'or paste using',
                  id: 'cTPeOs',
                })
              }}</span>
              <v-hotkey
                keys="cmd+v"
                platform="auto"
                variant="contained"
              />
            </div>
          </template>
        </v-file-upload-dropzone>
      </template>
    </v-file-upload>

    <v-container
      fluid
      class="pa-0"
    >
      <v-row :density="display.xs.value ? 'compact' : 'comfortable'">
        <v-col
          v-for="poster in posters"
          :key="isPosterDto(poster) ? poster.id : fileIdentifier(poster)"
          :cols="display.xs.value ? 6 : 'auto'"
        >
          <PosterCard
            :file="poster"
            :selected="isSelected(poster)"
            :to-delete="isPosterDto(poster) ? deleteQueue.some((it) => it.id === poster.id) : false"
            :width="display.xs.value ? 'auto' : 220"
            @deleted="handleDelete"
            @undeleted="handleUndelete"
            @selected="handleSelect"
          />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { useMessagesStore } from '@/stores/messages'
import { defineMessage, useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import {
  fileIdentifier,
  isPosterDto,
  POSTER_SIZE_LIMIT,
  type PosterDto,
  type PosterUpload,
} from '@/functions/poster'
import { useEventListener, watchImmediate } from '@vueuse/core'
import { useSettings } from '@/colada/settings'
import { extractImageFromClipboard } from '@/functions/clipboard'

const { entityPosters } = defineProps<{
  entityPosters: PosterDto[]
}>()

const emit = defineEmits<{
  uploadQueueChanged: [uploadQueue: PosterUpload[]]
  deleteQueueChanged: [deleteQueue: PosterDto[]]
  posterSelected: [selected: PosterDto | undefined]
}>()

const selection = ref<{ id: string; type: 'local' | 'remote' } | undefined>()
const uploadQueue = ref<File[]>([])
const deleteQueue = ref<PosterDto[]>([])

const messagesStore = useMessagesStore()
const display = useDisplay()
const intl = useIntl()

const posters = computed(() => [...entityPosters, ...uploadQueue.value] as (File | PosterDto)[])

const { data: serverSettings } = useSettings()
const posterSizeLimit = computed(
  () => serverSettings.value?.maxUploadFileSizeBytes ?? POSTER_SIZE_LIMIT,
)

// handle selection of remote poster on initialization
watchImmediate(
  () => entityPosters,
  (newEntityPosters) => {
    const selected = newEntityPosters.find((it) => it.selected)
    if (selected) selection.value = { id: selected.id, type: 'remote' }
  },
)

// handle selection fallback if the selected poster is deleted
watch([selection, deleteQueue], ([newSelectedPoster, newDeleteQueue]) => {
  // if no poster is selected, try to select the best candidate
  if (!newSelectedPoster) {
    // fallback to the selected one from the entity poster, if not marked for deletion
    const candidate1 = entityPosters.find((it) => it.selected)
    if (candidate1 && !newDeleteQueue.some((it) => it.id === candidate1.id))
      selection.value = { id: candidate1.id, type: 'remote' }
    // else select the first local file
    const candidate2 = uploadQueue.value.at(0)
    if (candidate2) selection.value = { type: 'local', id: fileIdentifier(candidate2) }
  }
})

// handle emit for deleteQueueChanged
watch(
  deleteQueue,
  (newDeleteQueue) => {
    emit('deleteQueueChanged', newDeleteQueue)
  },
  {
    immediate: true,
    deep: true,
  },
)

// handle emit for: uploadQueueChanged, posterSelected
watch(
  [selection, uploadQueue],
  ([newSelection, newUploadQueue]) => {
    emit(
      'uploadQueueChanged',
      newUploadQueue.map((it) => ({
        file: it,
        selected:
          (newSelection &&
            newSelection.type === 'local' &&
            fileIdentifier(it) === newSelection.id) ??
          false,
      })),
    )
    emit(
      'posterSelected',
      entityPosters.find((it) => it.id === newSelection?.id),
    )
  },
  {
    immediate: true,
    deep: true,
  },
)

// register paste event
useEventListener(window, 'paste', (event: ClipboardEvent) => {
  void handleClipboardItems(Array.from(event.clipboardData?.items ?? []))
  event.preventDefault()
})

function isSelected(poster: File | PosterDto): boolean {
  if (poster instanceof File)
    return selection.value?.type === 'local' && selection.value.id === fileIdentifier(poster)
  return selection.value?.type === 'remote' && selection.value.id === poster.id
}

function handleReject() {
  messagesStore.messages.push(
    defineMessage({
      description: 'Poster upload: error message when trying to upload an unsupported file type',
      defaultMessage: 'File type not supported',
      id: 'anhfpP',
    }),
  )
}

function updateLocalFiles(localFiles: File[]) {
  // remove duplicates
  const seen = new Set<string>()
  const distinctFiles = localFiles.filter((file) => {
    // Create a unique key using filename, size, and timestamp
    const identifier = fileIdentifier(file)

    if (seen.has(identifier)) {
      return false
    }

    seen.add(identifier)
    return true
  })

  // remove files over the size limit
  const validFiles: File[] = []
  const rejectedFiles: File[] = []
  for (const localFile of distinctFiles) {
    if (localFile.size > posterSizeLimit.value) rejectedFiles.push(localFile)
    else validFiles.push(localFile)
  }

  if (rejectedFiles.length > 0) {
    messagesStore.messages.push(
      intl.formatMessage(
        {
          description:
            'Poster upload: error message when trying to upload file over the size limit',
          defaultMessage: 'Files over the size limit: {filenames}',
          id: '+y+9xL',
        },
        {
          filenames: intl.formatList(
            rejectedFiles.map((it) => it.name),
            { type: 'unit' },
          ),
        },
      ),
    )
  }

  uploadQueue.value = validFiles

  // select the first local file
  const selectionCandidate = uploadQueue.value.at(0)
  if (selectionCandidate)
    selection.value = { type: 'local', id: fileIdentifier(selectionCandidate) }
}

function handleSelect(poster: File | PosterDto) {
  if (poster instanceof File) selection.value = { type: 'local', id: fileIdentifier(poster) }
  else selection.value = { type: 'remote', id: poster.id }
}

function handleDelete(poster: File | PosterDto) {
  if (poster instanceof File) {
    // remove local file from the upload queue
    uploadQueue.value = uploadQueue.value.filter((it) => it !== poster)
  } else {
    deleteQueue.value.push(poster)
  }

  // if it was selected, deselect
  if (isSelected(poster)) {
    selection.value = undefined
  }
}

function handleUndelete(poster: File | PosterDto) {
  if (isPosterDto(poster) && deleteQueue.value.some((it) => it.id === poster.id)) {
    deleteQueue.value = deleteQueue.value.filter((it) => it.id !== poster.id)
  }
}

async function handleClipboardItems(clipItems: ClipboardItem[] | DataTransferItem[]) {
  if (!clipItems || clipItems.length === 0) return

  const pastedFiles = (
    await Promise.all(clipItems.map((it) => extractImageFromClipboard(it)))
  ).filter((it) => it !== undefined)

  if (pastedFiles.length > 0) {
    updateLocalFiles([...uploadQueue.value, ...pastedFiles])
  } else {
    messagesStore.messages.push(
      defineMessage({
        description: 'Poster upload: error message when pasting from clipboard failed',
        defaultMessage: 'No valid image found in clipboard',
        id: 'wwiazg',
      }),
    )
  }
}
</script>
