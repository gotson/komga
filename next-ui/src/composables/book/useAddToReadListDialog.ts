import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { defineMessage, useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import AddToReadList from '@/components/readlist/AddTo.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import { isBook, isSeries } from '@/functions/entity'
import type { BookDto, ReadListDto, SeriesDto } from '@/generated/openapi'

export function useAddToReadListDialog() {
  const { simple: dialogSimple } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()

  const prepareDialog = (entities: (BookDto | SeriesDto)[], callback: () => void = () => {}) => {
    const bookIds = entities.filter((e) => isBook(e)).map((it) => it.id)
    const seriesIds = entities.filter((e) => isSeries(e)).map((it) => it.id)

    dialogSimple.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Add to read list dialog title',
        defaultMessage: 'Add to read list',
        id: 'Vs7y3+',
      }),
      // subtitle: book.metadata.title,
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
      component: markRaw(AddToReadList),
      props: {
        bookIds: bookIds,
        seriesIds: seriesIds,
        onCreated: (rl: ReadListDto) => {
          messagesStore.messages.push({
            message: commonMessages.readListCreated,
            action: {
              to: { name: '/readlist/[id]', params: { id: rl.id } },
              label: commonMessages.notificationOpen,
            },
          })

          dialogSimple.value.dialogProps.shown = false
          callback()
        },
        onAddedTo: (rl: ReadListDto) => {
          messagesStore.messages.push({
            message: defineMessage({
              description: 'Add to read list notification: books added to read list',
              defaultMessage: 'Added to readlist',
              id: 'dSy8IC',
            }),
            action: {
              to: { name: '/readlist/[id]', params: { id: rl.id } },
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
