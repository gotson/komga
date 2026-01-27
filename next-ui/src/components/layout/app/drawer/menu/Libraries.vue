<template>
  <v-list-item
    :title="
      $formatMessage({
        description: 'Drawer menu for Libraries',
        defaultMessage: 'Libraries',
        id: 'eyYZUe',
      })
    "
    prepend-icon="i-mdi:bookshelf"
    to="/libraries/pinned"
  >
    <template #append>
      <v-icon-btn
        v-if="isAdmin"
        icon="i-mdi:plus"
        variant="text"
        :aria-label="
          $formatMessage({
            description: 'Add library button: aria label',
            defaultMessage: 'add library',
            id: '90yqRq',
          })
        "
        @mouseenter="
          (event: Event) => (dialogConfirmEdit.activator = event.currentTarget as Element)
        "
        @click.prevent="createLibrary"
      />
      <v-icon-btn
        :id="id"
        icon="i-mdi:dots-vertical"
        variant="text"
        :aria-label="
          $formatMessage({
            description: 'Libraries menu button: aria label',
            defaultMessage: 'libraries menu',
            id: 'hJEc5M',
          })
        "
        @click.prevent
      />
      <LibraryMenuLibraries :activator-id="`#${id}`" />
    </template>
  </v-list-item>

  <LayoutAppDrawerMenuLibraryItem
    v-for="library in pinned"
    :key="library.id"
    :library="library"
  />

  <v-list-group
    v-if="unpinned.length > 0"
    value="Unpinned"
  >
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        prepend-icon="blank"
        :title="
          $formatMessage({
            description: 'Drawer menu for Unpinned libraries',
            defaultMessage: 'More',
            id: 'XDV3Si',
          })
        "
      />
    </template>

    <LayoutAppDrawerMenuLibraryItem
      v-for="library in unpinned"
      :key="library.id"
      :library="library"
    />
  </v-list-group>
</template>

<script setup lang="ts">
import { useCreateLibrary, useLibraries } from '@/colada/libraries'
import { useCurrentUser } from '@/colada/users'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import CreateEdit from '@/components/library/form/CreateEdit.vue'
import { getLibraryDefaults } from '@/modules/libraries'
import type { components } from '@/generated/openapi/komga'
import { useMessagesStore } from '@/stores/messages'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'

const intl = useIntl()
const display = useDisplay()
const { unpinned, pinned, refresh } = useLibraries()
const { isAdmin } = useCurrentUser()

const id = useId()

// ensure freshness, especially if libraries have been reordered
void refresh()

const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
const { mutateAsync: mutateCreateLibrary } = useCreateLibrary()
const messagesStore = useMessagesStore()

function createLibrary() {
  dialogConfirmEdit.value.dialogProps = {
    title: intl.formatMessage({
      description: 'Create library dialog title',
      defaultMessage: 'Create library',
      id: 'nuoJ1n',
    }),
    maxWidth: 600,
    okText: 'Create',
    cardTextClass: 'px-0',
    closeOnSave: false,
    scrollable: true,
    fullscreen: display.xs.value,
  }
  dialogConfirmEdit.value.slot = {
    component: markRaw(CreateEdit),
    props: { createMode: true },
  }
  dialogConfirmEdit.value.record = getLibraryDefaults()
  dialogConfirmEdit.value.callback = (
    hideDialog: () => void,
    setLoading: (isLoading: boolean) => void,
  ) => {
    setLoading(true)

    const newLib = dialogConfirmEdit.value.record as components['schemas']['LibraryCreationDto']

    mutateCreateLibrary(newLib)
      .then(() => {
        hideDialog()
        messagesStore.messages.push({
          text: intl.formatMessage(
            {
              description: 'Snackbar notification shown upon successful library creation',
              defaultMessage: 'Library created: {library}',
              id: '+8++PW',
            },
            {
              library: newLib.name,
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
</script>
