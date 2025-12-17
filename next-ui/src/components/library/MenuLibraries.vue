<template>
  <v-menu
    :activator="activatorId"
    location="end"
  >
    <v-list density="compact">
      <template
        v-for="(action, i) in actions"
        :key="i"
      >
        <v-divider v-if="action.divider" />
        <v-list-item
          v-else
          v-bind="action"
        />
      </template>
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { useAppStore } from '@/stores/app'
import { useEmptyTrashLibrary, useLibraries, useScanLibrary } from '@/colada/libraries'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useDisplay } from 'vuetify/framework'
import { commonMessages } from '@/utils/i18n/common-messages'

const { activatorId } = defineProps<{
  activatorId: string
}>()

const intl = useIntl()
const appStore = useAppStore()

const actions = [
  {
    title: intl.formatMessage({
      description: 'Libraries menu: reorder',
      defaultMessage: 'Reorder',
      id: 'EcIbyl',
    }),
    onClick: () => (appStore.reorderLibraries = true),
  },
  { divider: true },
  {
    title: intl.formatMessage({
      description: 'Libraries menu: scan',
      defaultMessage: 'Scan all libraries',
      id: 'CY8sfH',
    }),
    onClick: () => scanAll(),
  },
  {
    title: intl.formatMessage({
      description: 'Libraries menu: empty trash',
      defaultMessage: 'Empty trash for all libraries',
      id: 'CwteMk',
    }),
    onMouseenter: (event: Event) =>
      (dialogConfirm.value.activator = event.currentTarget as Element),
    onClick: () => emptyTrashAll(),
  },
]

const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
const display = useDisplay()

const { data: libraries } = useLibraries()

//region Scan
const { mutate: mutateScan } = useScanLibrary()

function scanAll() {
  libraries.value?.forEach((it) => mutateScan({ libraryId: it.id }))
}
//endregion

//region Empty Trash
const { mutate: mutateEmptyTrash } = useEmptyTrashLibrary()

function emptyTrashAll() {
  dialogConfirm.value.dialogProps = {
    title: intl.formatMessage(commonMessages.dialogEmptyTrashTitle),
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
  dialogConfirm.value.callback = () => {
    libraries.value?.forEach((it) => mutateEmptyTrash(it.id))
  }
}
//endregion
</script>

<script lang="ts"></script>

<style scoped></style>
