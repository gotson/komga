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

export function useEditSeriesMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()

  const prepareDialog = (series: SeriesDto, callback: () => void = () => {}) => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit series metadata dialog title',
        defaultMessage: 'Edit series metadata',
        id: '1bxWGd',
      }),
      subtitle: series.metadata.title,
      maxWidth: 1000,
      cardTextProps: {
        class: 'px-0',
      },
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(EditSeries),
    }
    dialogConfirmEdit.value.record = createEntityUpdate(series)
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

      const updatedData = dialogConfirmEdit.value.record as EntityUpdate<SeriesDto>

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
      mutateUpdateSeriesMetadata({ seriesId: series.id, metadata: updateDto })
        .then(() => {
          hideDialog()
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
          setLoading(false)
        })
        .finally(() => callback())
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
