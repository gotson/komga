<template>
  <v-container fluid>
    <v-row align="center">
      <v-col
        cols="12"
        sm=""
      >
        <v-text-field
          v-model="appStore.importBooksPath"
          hide-details
          clearable
          :label="
            $formatMessage({
              description: 'Import books directory selection: directory text field label',
              defaultMessage: 'Import from directory',
              id: 'z8b1Xe',
            })
          "
        />
      </v-col>

      <v-col cols="auto">
        <v-btn
          color=""
          @click="browse"
          @mouseenter="dialogConfirmEdit.activator = $event.currentTarget"
          >{{
            $formatMessage({
              description: 'Import books directory selection: file browser button label',
              defaultMessage: 'Browse',
              id: 'Usohru',
            })
          }}
        </v-btn>
      </v-col>

      <v-col cols="auto">
        <v-btn
          :disabled="!appStore.importBooksPath || loading"
          :text="
            $formatMessage({
              description: 'Import books: scan button label',
              defaultMessage: 'Scan',
              id: 'uwFE74',
            })
          "
          @click="emit('scan', appStore.importBooksPath)"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import FragmentRemoteFileList from '@/fragments/fragment/RemoteFileList.vue'
import { useDisplay } from 'vuetify'
import { useAppStore } from '@/stores/app'
import { useIntl } from 'vue-intl'

const intl = useIntl()
const display = useDisplay()
const appStore = useAppStore()

const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())

const { loading = false } = defineProps<{
  loading?: boolean
}>()

const emit = defineEmits<{
  scan: [directory: string]
}>()

function browse() {
  dialogConfirmEdit.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Import books: directory selection dialog title',
      defaultMessage: 'Import from directory',
      id: '8Om/o/',
    }),
    maxWidth: 600,
    closeOnSave: true,
    scrollable: true,
    fullscreen: display.xs.value,
  }
  dialogConfirmEdit.value.slot = {
    component: markRaw(FragmentRemoteFileList),
    props: {},
  }
  dialogConfirmEdit.value.record = appStore.importBooksPath || '' // workaround for https://github.com/vuetifyjs/vuetify/issues/4144
  dialogConfirmEdit.value.callback = () => {
    appStore.importBooksPath = dialogConfirmEdit.value.record as string
  }
}
</script>
