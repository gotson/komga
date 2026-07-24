<template>
  <Teleport
    to="#navbar-append-teleport-target"
    defer
  >
    <PosterSizeSlider />

    <v-icon-btn
      v-tooltip:bottom="$formatMessage(editMessage)"
      icon="i-mdi:star-cog"
      @mouseenter="(event: Event) => (dialogConfirmEdit.activator = event.currentTarget as Element)"
      @click="editSections()"
    />
  </Teleport>

  <div class="pa-4 d-flex flex-column ga-6">
    <OverviewSection
      v-for="section in overviewSections"
      :key="section.section"
      :section="section"
      :library-view-id="libraryViewId"
    />

    <v-empty-state
      v-if="overviewSections.length === 0"
      :text="
        $formatMessage({
          description: 'Overview: empty state text when no sections are configured',
          defaultMessage: 'No sections configured',
          id: 'ZE3XpF',
        })
      "
      icon="i-mdi:star-cog"
      color="secondary"
    >
      <template #actions>
        <v-btn
          :text="$formatMessage(editMessage)"
          color=""
          @mouseenter="
            (event: Event) => (dialogConfirmEdit.activator = event.currentTarget as Element)
          "
          @click="editSections()"
        />
      </template>
    </v-empty-state>
  </div>
</template>

<script lang="ts" setup>
import { OverviewSectionsDefault } from '@/types/OverviewSection'
import PosterSizeSlider from '@/components/PosterSizeSlider.vue'
import { useClientSettingsUser, useUpdateClientSettingsUser } from '@/colada/client-settings'
import {
  ClientSettingUser,
  type ClientSettingUserOverviewSection,
} from '@/types/ClientSettingsUser'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { commonMessages } from '@/utils/i18n/common-messages'
import { defineMessage, useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import ReorderOverviewSections from '@/components/ReorderOverviewSections.vue'
import { useMessagesStore } from '@/stores/messages'

const intl = useIntl()
const display = useDisplay()
const route = useRoute('/libraries/[id]/readlists')
const libraryViewId = route.params.id

const { userSettings } = useClientSettingsUser()
const overviewSections = computed(() => {
  return (
    userSettings.value[ClientSettingUser.NextUIOverviewSections]?.[libraryViewId] ??
    OverviewSectionsDefault
  )
})

const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
const { mutateAsync } = useUpdateClientSettingsUser()
const messagesStore = useMessagesStore()

function editSections() {
  dialogConfirmEdit.value.dialogProps = {
    title: intl.formatMessage(editMessage),
    maxWidth: 500,
    okText: 'Save',
    cardTextClass: 'px-0',
    closeOnSave: false,
    scrollable: false,
    fullscreen: display.xs.value,
  }
  dialogConfirmEdit.value.slot = {
    component: markRaw(ReorderOverviewSections),
  }
  dialogConfirmEdit.value.record = toValue(overviewSections)
  dialogConfirmEdit.value.callback = (
    hideDialog: () => void,
    setLoading: (isLoading: boolean) => void,
  ) => {
    setLoading(true)

    const updatedSections = dialogConfirmEdit.value.record as ClientSettingUserOverviewSection[]
    const newSettings = { ...userSettings.value[ClientSettingUser.NextUIOverviewSections] }
    newSettings[libraryViewId] = updatedSections

    mutateAsync({ [ClientSettingUser.NextUIOverviewSections]: newSettings })
      .then(() => {
        hideDialog()
      })
      .catch((error) => {
        messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        setLoading(false)
      })
  }
}

const editMessage = defineMessage({
  description: 'Edit overview sections: title',
  defaultMessage: 'Edit view',
  id: '3J6FI8',
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
