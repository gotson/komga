import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { defineMessage, useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import AddToCollection from '@/components/collection/AddTo.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { CollectionDto } from '@/generated/openapi'

export function useAddToCollectionDialog() {
  const { simple: dialogSimple } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()

  const prepareDialog = (seriesIds: string[], callback: () => void = () => {}) => {
    dialogSimple.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Add to collection dialog title',
        defaultMessage: 'Add to collection',
        id: 'e7cgeE',
      }),
      maxWidth: 600,
      maxHeight: '80vh',
      cardTextProps: {
        class: 'd-flex flex-column flex-grow-1 overflow-hidden',
        style: 'min-height: 0',
      },
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogSimple.value.callback = () => callback()
    dialogSimple.value.slot = {
      component: markRaw(AddToCollection),
      props: {
        seriesIds: seriesIds,
        onCreated: (col: CollectionDto) => {
          messagesStore.messages.push({
            message: defineMessage({
              description: 'Create collection notification: collection created',
              defaultMessage: 'Collection created',
              id: 'CAKrBF',
            }),
            action: {
              to: { name: '/collection/[id]', params: { id: col.id } },
              label: commonMessages.notificationOpen,
            },
          })

          dialogSimple.value.dialogProps.shown = false
          callback()
        },
        onAddedTo: (col: CollectionDto) => {
          messagesStore.messages.push({
            message: defineMessage({
              description: 'Add to collection notification: series added to collection',
              defaultMessage: 'Added to collection',
              id: 'WSTiWA',
            }),
            action: {
              to: { name: '/collection/[id]', params: { id: col.id } },
              label: commonMessages.notificationOpen,
            },
          })

          dialogSimple.value.dialogProps.shown = false
          callback()
        },
      },
    }
  }

  const activatorRef = computed({
    get: () => dialogSimple.value.activator,
    set: (val) => (dialogSimple.value.activator = val),
  })

  function showDialog() {
    dialogSimple.value.dialogProps.shown = true
  }

  return {
    prepareDialog: prepareDialog,
    activator: activatorRef,
    showDialog: showDialog,
  }
}
