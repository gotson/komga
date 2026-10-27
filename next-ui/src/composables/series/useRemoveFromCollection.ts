import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useQuery } from '@pinia/colada'
import { collectionDetailQuery, useUpdateCollection } from '@/colada/collections'
import { pick } from '@/functions/object'

export function useRemoveFromCollection() {
  const { confirm: dialogConfirm } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()

  // get the collection so we can get the list of series
  const fetchCollectionId = ref('none')
  const { data: collection, refresh } = useQuery(() => ({
    ...collectionDetailQuery({ collectionId: fetchCollectionId.value }),
    enabled: fetchCollectionId.value !== 'none',
  }))

  const { mutateAsync: mutateUpdate } = useUpdateCollection()

  const prepareDialog = async (
    seriesIds: string[],
    collectionId: string,
    callback: () => void = () => {},
  ) => {
    // trigger fetching
    fetchCollectionId.value = collectionId
    await refresh()

    dialogConfirm.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Remove from collection dialog: title',
        defaultMessage: 'Remove from collection',
        id: 'tc55Tn',
      }),
      subtitle: collection.value?.name,
      maxWidth: 600,
      mode: 'checkbox',
      color: 'primary',
      okText: intl.formatMessage({
        description: 'Remove from collection dialog: confirm button',
        defaultMessage: 'Remove',
        id: '98Q8V7',
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
              description: 'Remove from collection dialog: warning',
              defaultMessage: `{count, plural,
one {# series}
other {# series}
} will be removed`,
              id: 'yvQfma',
            },
            {
              count: seriesIds.length,
            },
          ),
        ),
      ),
      props: {},
    }
    dialogConfirm.value.callback = (result: DialogResult) => {
      if (result === 'confirm' && collection.value) {
        const updateDto = pick(collection.value, 'seriesIds')
        updateDto.seriesIds = updateDto.seriesIds.filter((it) => !seriesIds.includes(it))

        if (updateDto.seriesIds.length > 0) {
          mutateUpdate({ collectionId: collectionId, data: updateDto })
            .then(() => {
              messagesStore.messages.push({
                message: intl.formatMessage(
                  {
                    description: 'Snackbar notification shown upon successful collection update',
                    defaultMessage: `{count, plural,
one {# series}
other {# series}
} removed from collection: {collection}`,
                    id: 'FX2bI8',
                  },
                  {
                    collection: collection.value!.name,
                    count: seriesIds.length,
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
              description: 'Snackbar notification shown upon failed collection update',
              defaultMessage: 'Collection must contain at least one series',
              id: '90k6a7',
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
