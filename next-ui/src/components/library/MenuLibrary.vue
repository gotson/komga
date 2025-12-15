<template>
  <v-menu
    :activator="activatorId"
    location="end"
  >
    <v-list density="compact">
      <v-list-item
        v-for="(action, i) in actions"
        :key="i"
        v-bind="action"
      />

      <v-list-item
        :title="
          $formatMessage({
            description: 'Library menu: manage',
            defaultMessage: 'Manage library',
            id: 'HNu1rT',
          })
        "
        append-icon="i-mdi:menu-right"
      >
        <v-menu
          activator="parent"
          open-on-click
          open-on-hover
          location="end"
          submenu
        >
          <v-list density="compact">
            <v-list-item
              v-for="(action, i) in manageActions"
              :key="i"
              v-bind="action"
            />
          </v-list>
        </v-menu>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import {
  useAnalyzeLibrary,
  useDeleteLibrary,
  useEmptyTrashLibrary,
  useRefreshMetadataLibrary,
  useScanLibrary,
  useUpdateLibrary,
} from '@/colada/libraries'
import { useMessagesStore } from '@/stores/messages'
import CreateEdit from '@/components/library/form/CreateEdit.vue'
import type { components } from '@/generated/openapi/komga'
import { useDisplay } from 'vuetify'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import LibraryDeletionWarning from '@/components/library/DeletionWarning.vue'

const { activatorId, library } = defineProps<{
  activatorId: string
  library: components['schemas']['LibraryDto']
}>()

const intl = useIntl()
const { confirmEdit: dialogConfirmEdit, confirm: dialogConfirm } = storeToRefs(useDialogsStore())
const messagesStore = useMessagesStore()
const display = useDisplay()

//region Actions
const actions = [
  {
    title: intl.formatMessage({
      description: 'Library menu: scan',
      defaultMessage: 'Scan library files',
      id: 'GCwZB2',
    }),
    onClick: () => scanLibrary(),
  },
]

const manageActions = [
  {
    title: intl.formatMessage({
      description: 'Library menu: manage > edit',
      defaultMessage: 'Edit',
      id: 'n4w2CE',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirmEdit.value.activator = event.currentTarget as Element),
    onClick: () => updateLibrary(),
  },
  {
    title: intl.formatMessage({
      description: 'Library menu: manage > deep scan',
      defaultMessage: 'Deep scan',
      id: 'foSDIW',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirm.value.activator = event.currentTarget as Element),
    onClick: () => scanDeep(),
  },
  {
    title: intl.formatMessage({
      description: 'Library menu: manage > refresh all metadata',
      defaultMessage: 'Refresh all metadata',
      id: 'OUyleX',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirm.value.activator = event.currentTarget as Element),
    onClick: () => refreshMetadata(),
  },
  {
    title: intl.formatMessage({
      description: 'Library menu: manage > empty trash',
      defaultMessage: 'Empty trash',
      id: 'sdNz1F',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirm.value.activator = event.currentTarget as Element),
    onClick: () => emptyTrash(),
  },
  {
    title: intl.formatMessage({
      description: 'Library menu: manage > analyze',
      defaultMessage: 'Analyze',
      id: 'E5ZMyt',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirm.value.activator = event.currentTarget as Element),
    onClick: () => analyzeLibrary(),
  },
  {
    title: intl.formatMessage({
      description: 'Library menu: manage > delete',
      defaultMessage: 'Delete',
      id: 'LFf8QB',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirm.value.activator = event.currentTarget as Element),
    onClick: () => deleteLibrary(),
  },
]
//endregion

//region Update Library
const { mutateAsync: mutateUpdateLibrary } = useUpdateLibrary()

function updateLibrary() {
  dialogConfirmEdit.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Update library dialog title',
      defaultMessage: 'Update library',
      id: 'am3r7e',
    }),
    maxWidth: 600,
    okText: 'Save',
    cardTextClass: 'px-0',
    closeOnSave: false,
    scrollable: true,
    fullscreen: display.xs.value,
  }
  dialogConfirmEdit.value.slot = {
    component: markRaw(CreateEdit),
    props: { createMode: false },
  }
  dialogConfirmEdit.value.record = library
  dialogConfirmEdit.value.callback = (
    hideDialog: () => void,
    setLoading: (isLoading: boolean) => void,
  ) => {
    setLoading(true)

    const updatedLib = dialogConfirmEdit.value.record as components['schemas']['LibraryDto']

    mutateUpdateLibrary(updatedLib)
      .then(() => {
        hideDialog()
        messagesStore.messages.push({
          text: intl.formatMessage(
            {
              description: 'Snackbar notification shown upon successful library update',
              defaultMessage: 'Library updated: {library}',
              id: 'aOiU5f',
            },
            {
              library: updatedLib.name,
            },
          ),
        })
      })
      .catch((error) => {
        messagesStore.messages.push({
          text:
            (error?.cause as ErrorCause)?.message ||
            intl.formatMessage(commonMessages.networkError),
        })
        setLoading(false)
      })
  }
}
//endregion

//region Refresh Metadata
const { mutate: mutateRefreshMetadata } = useRefreshMetadataLibrary()

function refreshMetadata() {
  dialogConfirm.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Library refresh metadata dialog: title',
      defaultMessage: 'Refresh all metadata',
      id: 'xiyw1J',
    }),
    subtitle: library.name,
    maxWidth: 600,
    mode: 'click',
    color: 'primary',
    okText: intl.formatMessage({
      description: 'Library refresh metadata dialog: confirm button',
      defaultMessage: 'Refresh',
      id: 'i+kSy9',
    }),
    closeOnSave: true,
    fullscreen: display.xs.value,
  }
  dialogConfirm.value.slotWarning = {
    component: markRaw(
      h(
        'div',
        intl.formatMessage({
          description: 'Library refresh metadata dialog: warning text',
          defaultMessage:
            'Refreshes metadata for all the media files in the library. Depending on your library size, this may take a long time.',
          id: 'vs88Ef',
        }),
      ),
    ),
    props: {},
  }
  dialogConfirm.value.callback = () => mutateRefreshMetadata(library.id)
}
//endregion

//region Empty Trash
const { mutate: mutateEmptyTrash } = useEmptyTrashLibrary()

function emptyTrash() {
  dialogConfirm.value.dialogProps = {
    title: intl.formatMessage(commonMessages.dialogEmptyTrashTitle),
    subtitle: library.name,
    maxWidth: 600,
    mode: 'click',
    color: 'primary',
    okText: intl.formatMessage(commonMessages.dialogEmptyTrashConfirm),
    closeOnSave: true,
    fullscreen: display.xs.value,
  }
  dialogConfirm.value.slotWarning = {
    component: markRaw(h('div', intl.formatMessage(commonMessages.dialogEmptyTrashNotice))),
    props: {},
  }
  dialogConfirm.value.callback = () => mutateEmptyTrash(library.id)
}
//endregion

//region Analyze
const { mutate: mutateAnalyze } = useAnalyzeLibrary()

function analyzeLibrary() {
  dialogConfirm.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Library analyze dialog: title',
      defaultMessage: 'Analyze library',
      id: '5JGOUU',
    }),
    subtitle: library.name,
    maxWidth: 600,
    mode: 'click',
    color: 'primary',
    okText: intl.formatMessage({
      description: 'Library analyze dialog: confirm button',
      defaultMessage: 'Analyze',
      id: 'jN3N1Q',
    }),
    closeOnSave: true,
    fullscreen: display.xs.value,
  }
  dialogConfirm.value.slotWarning = {
    component: markRaw(
      h(
        'div',
        intl.formatMessage({
          description: 'Library empty trash dialog: warning text',
          defaultMessage:
            'Analyzes all the media files in the library. The analysis captures information about the media. Depending on your library size, this may take a long time.',
          id: '8xonXJ',
        }),
      ),
    ),
    props: {},
  }
  dialogConfirm.value.callback = () => mutateAnalyze(library.id)
}
//endregion

//region Scan
const { mutate: mutateScan } = useScanLibrary()

function scanLibrary() {
  mutateScan({ libraryId: library.id })
}

function scanDeep() {
  dialogConfirm.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Library deep scan dialog: title',
      defaultMessage: 'Deep scan',
      id: 'hV3EW+',
    }),
    subtitle: library.name,
    maxWidth: 600,
    mode: 'click',
    color: 'primary',
    okText: intl.formatMessage({
      description: 'Library deep scan: confirm button',
      defaultMessage: 'Deep scan',
      id: 'OxqfKF',
    }),
    closeOnSave: true,
    fullscreen: display.xs.value,
  }
  dialogConfirm.value.slotWarning = {
    component: markRaw(
      h(
        'div',
        intl.formatMessage({
          description: 'Library deep scan dialog: warning text',
          defaultMessage:
            'Performs a deep scan of the library files. Depending on your library size, this may take a long time.',
          id: 'y3nPgO',
        }),
      ),
    ),
    props: {},
  }
  dialogConfirm.value.callback = () => mutateScan({ libraryId: library.id, deep: true })
}
//endregion

//region Delete
const { mutateAsync: mutateDelete } = useDeleteLibrary()

function deleteLibrary() {
  dialogConfirm.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Library delete dialog: title',
      defaultMessage: 'Delete library',
      id: '3T1ln7',
    }),
    subtitle: library.name,
    maxWidth: 600,
    mode: 'textinput',
    color: 'error',
    validateText: library.name,
    okText: intl.formatMessage({
      description: 'Library delete dialog: confirm button',
      defaultMessage: 'Delete',
      id: '/5Gb4y',
    }),
    closeOnSave: true,
    fullscreen: display.xs.value,
  }
  dialogConfirm.value.slotWarning = {
    component: markRaw(LibraryDeletionWarning),
    props: {},
  }
  dialogConfirm.value.callback = () => {
    mutateDelete(library.id)
      .then(() => {
        messagesStore.messages.push({
          text: intl.formatMessage(
            {
              description: 'Snackbar notification shown upon successful library deletion',
              defaultMessage: 'Library deleted: {library}',
              id: 'PvKF7E',
            },
            {
              library: library.name,
            },
          ),
        })
      })
      .catch((error) => {
        messagesStore.messages.push({
          text:
            (error?.cause as ErrorCause)?.message ||
            intl.formatMessage(commonMessages.networkError),
        })
      })
  }
}
//endregion
</script>

<script lang="ts"></script>

<style scoped></style>
