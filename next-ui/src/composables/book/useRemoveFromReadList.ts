import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { BookDto } from '@/generated/openapi'
import { useQuery } from '@pinia/colada'
import { readListDetailQuery, useUpdateReadList } from '@/colada/readlists'
import { pick } from '@/functions/object'

export function useRemoveFromReadList() {
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()

  // get the read list so we can get the list of books
  const fetchReadListId = ref('none')
  const { data: readList, refresh } = useQuery(() => ({
    ...readListDetailQuery({ readListId: fetchReadListId.value }),
    enabled: fetchReadListId.value !== 'none',
  }))

  const { mutateAsync: mutateUpdate } = useUpdateReadList()

  const prepareDialog = async (
    entities: BookDto[],
    readListId: string,
    callback: () => void = () => {},
  ) => {
    // trigger fetching
    fetchReadListId.value = readListId
    await refresh()

    const idsToRemove = entities.map((it) => it.id)

    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Remove from read list dialog: title',
        defaultMessage: 'Remove from read list',
        id: '69jd/r',
      }),
      subtitle: readList.value?.name,
      maxWidth: 600,
      mode: 'checkbox',
      color: 'primary',
      okText: intl.formatMessage({
        description: 'Remove from read list dialog: confirm button',
        defaultMessage: 'Remove',
        id: 'lu0qxx',
      }),
      closeOnSave: true,
      fullscreen: display.xs.value,
    }
    dialogConfirm.value.slotWarning = {
      component: markRaw(
        h(
          'div',
          intl.formatMessage(
            {
              description: 'Remove from read list dialog: warning',
              defaultMessage: `{count, plural,
one {# book}
other {# books}
} will be removed`,
              id: 'tEA65+',
            },
            {
              count: entities.length,
            },
          ),
        ),
      ),
      props: {},
    }
    dialogConfirm.value.callback = (result: DialogResult) => {
      if (result === 'confirm' && readList.value) {
        const updateDto = pick(readList.value, 'bookIds')
        updateDto.bookIds = updateDto.bookIds.filter((it) => !idsToRemove.includes(it))

        if (updateDto.bookIds.length > 0) {
          mutateUpdate({ readListId: readListId, data: updateDto })
            .then(() => {
              messagesStore.messages.push({
                message: intl.formatMessage(
                  {
                    description: 'Snackbar notification shown upon successful readlist update',
                    defaultMessage: `{count, plural,
one {# book}
other {# books}
} removed from read list: {readlist}`,
                    id: 'tFx9of',
                  },
                  {
                    readlist: readList.value!.name,
                    count: entities.length,
                  },
                ),
              })
            })
            .catch((error) => {
              messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
            })
        } else {
          messagesStore.messages.push(
            intl.formatMessage({
              description: 'Snackbar notification shown upon failed readlist update',
              defaultMessage: 'Read list must contain at least one book',
              id: 'RCUCFa',
            }),
          )
        }
      }

      callback()
    }
  }

  const activatorRef = computed({
    get: () => dialogConfirm.value.activator,
    set: (val) => (dialogConfirm.value.activator = val),
  })

  function showDialog() {
    dialogConfirm.value.dialogProps.shown = true
  }

  return {
    prepareDialog: prepareDialog,
    activator: activatorRef,
    showDialog: showDialog,
  }
}
