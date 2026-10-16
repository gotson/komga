import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import {
  useAddSeriesPoster,
  useDeleteSeriesPoster,
  useMarkSeriesPosterSelected,
  useUpdateSeriesMetadata,
} from '@/colada/series'

import EditSeries from '@/components/series/form/Edit.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { SeriesDto } from '@/generated/openapi'
import { createEntityUpdate, type EntityUpdate } from '@/functions/poster'
import PrevNext from '@/components/item/form/PrevNext.vue'

export function useEditSeriesMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()

  const prepareDialog = (series: SeriesDto[], callback: () => void = () => {}) => {
    // use a ref so we can cycle series that have been modified
    const seriesToEdit = ref(series)

    // display first series
    displayOne(seriesToEdit.value.at(0)!, seriesToEdit)

    dialogConfirmEdit.value.callback = async (
      result: DialogResult,
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      if (result === 'cancel') {
        callback()
        return
      }

      setLoading(true)

      const updatedData = dialogConfirmEdit.value.record as EntityUpdate<SeriesDto, never>
      // keep track in the ref
      seriesToEdit.value = seriesToEdit.value.map((it) =>
        it.id !== updatedData.entity.id ? it : updatedData.entity,
      )
      // update server
      await saveOne(updatedData)

      const seriesIndex = seriesToEdit.value.findIndex((it) => it.id === updatedData.entity.id)

      // if it's the last series, close dialog
      if (seriesIndex === seriesToEdit.value.length - 1) {
        hideDialog()
        callback()
      } else {
        // display next book
        displayOne(seriesToEdit.value.at(seriesIndex + 1)!, seriesToEdit)
        setLoading(false)
      }
    }

    function displayOne(current: SeriesDto, series: Ref<SeriesDto[]>) {
      const isMultiple = series.value.length > 1
      const isLast = series.value.at(-1)?.id === current.id

      dialogConfirmEdit.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Edit series metadata dialog title',
          defaultMessage: 'Edit series metadata',
          id: '1bxWGd',
        }),
        subtitle: current.metadata.title,
        maxWidth: 1000,
        cardTextProps: {
          class: 'px-0',
        },
        cancelText: isMultiple
          ? intl.formatMessage({
              description: 'Edit series metadata dialog: close button text',
              defaultMessage: 'Close',
              id: '3ScD7a',
            })
          : undefined,
        okText: isMultiple
          ? isLast
            ? intl.formatMessage({
                description:
                  'Edit multiple series metadata dialog: confirmation button text for last series',
                defaultMessage: 'Save and close',
                id: 'bBgvnt',
              })
            : intl.formatMessage({
                description:
                  'Edit multiple series metadata dialog: confirmation button text for all series except last',
                defaultMessage: 'Save and next',
                id: '9uC2jO',
              })
          : undefined,
        closeOnSave: false,
        scrollable: true,
        fullscreen: display.xs.value,
      }

      dialogConfirmEdit.value.slots = {
        default: {
          component: markRaw(EditSeries),
        },
        prependActions: isMultiple
          ? {
              component: markRaw(PrevNext),
              props: {
                items: series,
                current: current,
              },
              handlers: {
                previous: (item: SeriesDto) => displayOne(item, series),
                next: (item: SeriesDto) => displayOne(item, series),
              },
            }
          : undefined,
      }

      dialogConfirmEdit.value.record = createEntityUpdate(current)
    }

    async function saveOne(updatedData: EntityUpdate<SeriesDto, never>) {
      // upload new posters
      if (updatedData.uploadQueue.length > 0) {
        const { mutateAsync } = useAddSeriesPoster()
        for (const newPoster of updatedData.uploadQueue) {
          await mutateAsync({
            seriesId: updatedData.entity.id,
            file: newPoster.file,
            selected: newPoster.selected,
          }).catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
        }
      }

      // mark existing poster as selected
      if (updatedData.selected) {
        const { mutateAsync } = useMarkSeriesPosterSelected()
        await mutateAsync({
          seriesId: updatedData.entity.id,
          thumbnailId: updatedData.selected.id,
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }

      // delete posters
      if (updatedData.deleteQueue.length > 0) {
        const { mutateAsync } = useDeleteSeriesPoster()
        for (const posterToDelete of updatedData.deleteQueue) {
          await mutateAsync({
            seriesId: updatedData.entity.id,
            thumbnailId: posterToDelete.id,
          }).catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
        }
      }

      // update series
      const updateDto = updatedData.entity.metadata
      await mutateUpdateSeriesMetadata({ seriesId: updatedData.entity.id, metadata: updateDto })
        .then(() => {
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful series metadata update',
                defaultMessage: 'Series metadata updated: {series}',
                id: 'gEBeQv',
              },
              {
                series: updateDto.title,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
    }
  }

  const activatorRef = computed({
    get: () => dialogConfirmEdit.value.activator,
    set: (val) => (dialogConfirmEdit.value.activator = val),
  })

  function showDialog() {
    dialogConfirmEdit.value.dialogProps.shown = true
  }

  return {
    prepareDialog: prepareDialog,
    activator: activatorRef,
    showDialog: showDialog,
  }
}
