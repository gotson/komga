import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import { useUpdateSeriesMetadata } from '@/colada/series'

import EditSeries from '@/components/series/form/bulk/Edit.vue'
import EmptyState from '@/components/EmptyStateLoading.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { BookDto, SeriesDto } from '@/generated/openapi'
import {
  type BulkUpdateSeries,
  BulkUpdateSeriesFields,
  type EntitiesUpdateBulk,
} from '@/types/entityUpdate'
import { applyBulkUpdates } from '@/functions/bulkEdit'
import { getFirstBookInParent } from '@/functions/book-container'
import { seriesMetadataToUpdateDto } from '@/functions/series'
import { omit, pick } from '@/functions/object'
import { usePosterUpdate } from '@/composables/entity/usePosterUpdate'

export function useBulkEditSeriesMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()
  const { updatePoster } = usePosterUpdate()

  const prepareDialog = (series: SeriesDto[], callback: () => void = () => {}) => {
    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(EmptyState),
      },
    }

    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit bulk series metadata dialog title',
        defaultMessage: 'Edit series metadata',
        id: 'WyueEI',
      }),
      subtitle: intl.formatMessage(
        {
          description: 'Edit bulk series metadata dialog subtitle',
          defaultMessage: `{first} and {rest, plural,
one {# other series}
other {# other series}
}`,
          id: 'TCjAyg',
        },
        {
          first: series.at(0)?.metadata.title,
          rest: series.length - 1,
        },
      ),
      maxWidth: 1000,
      cardTextProps: {
        class: 'px-0',
      },
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }

    dialogConfirmEdit.value.record = {
      entities: series.map((it) => ({ entity: it })),
      bulkUpdates: {
        status: undefined,
        statusLock: undefined,
        language: undefined,
        languageLock: undefined,
        readingDirection: undefined,
        readingDirectionLock: undefined,
        publisher: undefined,
        publisherLock: undefined,
        ageRating: undefined,
        ageRatingLock: undefined,
        genres: [],
        genresLock: undefined,
        tags: [],
        tagsLock: undefined,
        sharingLabels: [],
        sharingLabelsLock: undefined,
      },
      uploadQueue: [],
    } satisfies EntitiesUpdateBulk<SeriesDto, never, BulkUpdateSeries>

    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(EditSeries),
      },
    }

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

      const updatedData = dialogConfirmEdit.value.record as EntitiesUpdateBulk<
        SeriesDto,
        never,
        BulkUpdateSeries
      >

      // update series
      let updateCount = 0
      for (const s of series) {
        // upload new posters
        let entity: SeriesDto | BookDto = s
        if (s.oneshot) {
          const book = await getFirstBookInParent(s, false)
          if (book) entity = book
        }
        await updatePoster(entity, pick(updatedData, 'uploadQueue'))

        // build update DTO
        let updateDto = pick(
          seriesMetadataToUpdateDto(applyBulkUpdates(s.metadata, updatedData.bulkUpdates)),
          ...BulkUpdateSeriesFields,
        )
        if (s.oneshot) {
          updateDto = { ...omit(updateDto, 'statusLock'), status: 'ENDED' }
        }

        await mutateUpdateSeriesMetadata({
          seriesId: s.id,
          metadata: updateDto,
        })
          .then(() => {
            updateCount++
          })
          .catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
      }

      messagesStore.messages.push({
        message: intl.formatMessage(
          {
            description: 'Snackbar notification shown upon successful bulk series metadata update',
            defaultMessage: '{count, plural, one {# series} other {# series} } updated',
            id: '+mvfpU',
          },
          {
            count: updateCount,
          },
        ),
      })

      hideDialog()
      callback()
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
