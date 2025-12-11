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
              :title="
                $formatMessage({
                  description: 'Library menu: manage > edit',
                  defaultMessage: 'Edit',
                  id: 'n4w2CE',
                })
              "
              @mouseenter="dialogConfirmEdit.activator = $event.currentTarget"
              @click="updateLibrary()"
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
import { useUpdateLibrary } from '@/colada/libraries'
import { useMessagesStore } from '@/stores/messages'
import CreateEdit from '@/components/library/form/CreateEdit.vue'
import type { components } from '@/generated/openapi/komga'
import { useDisplay } from 'vuetify'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'

const { activatorId, library } = defineProps<{
  activatorId: string
  library: components['schemas']['LibraryDto']
}>()

const intl = useIntl()
const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
const messagesStore = useMessagesStore()
const display = useDisplay()

//region actions
const actions = [
  {
    title: intl.formatMessage({
      description: 'Library menu: scan',
      defaultMessage: 'Scan library files',
      id: 'GCwZB2',
    }),
    // onClick: () => (appStore.reorderLibraries = true),
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
</script>

<script lang="ts"></script>

<style scoped></style>
