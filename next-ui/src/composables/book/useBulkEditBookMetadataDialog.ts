import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import { useUpdateSeriesMetadata } from '@/colada/series'

import EditBooks from '@/components/book/form/bulk/Edit.vue'
import EmptyState from '@/components/EmptyStateLoading.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { BookDto, BookMetadataUpdateDto, SeriesDto } from '@/generated/openapi'
import {
  type BulkUpdateBook,
  BulkUpdateBookFields,
  BulkUpdateBookOneShotFields,
  type EntitiesUpdateBulk,
} from '@/types/entityUpdate'
import { applyBulkUpdates } from '@/functions/bulkEdit'
import { useUpdateBookMetadata } from '@/colada/books'
import type { OneShotAttributesBulk } from '@/types/oneshot'
import { usePosterUpdate } from '@/composables/entity/usePosterUpdate'
import { pick } from '@/functions/object'
import { getParentSeries } from '@/functions/book'
import { seriesMetadataToUpdateDto } from '@/functions/series'

export function useBulkEditBookMetadataDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()
  const { mutateAsync: mutateUpdateBookMetadata } = useUpdateBookMetadata()
  const { updatePoster } = usePosterUpdate()

  const prepareDialog = async (books: BookDto[], callback: () => void = () => {}) => {
    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(EmptyState),
      },
    }

    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Edit bulk book metadata dialog title',
        defaultMessage: 'Edit books metadata',
        id: 'dNnqm2',
      }),
      subtitle: intl.formatMessage(
        {
          description: 'Edit bulk book metadata dialog subtitle',
          defaultMessage: `{first} and {rest, plural,
one {# other book}
other {# other books}
}`,
          id: 'Rt/kZN',
        },
        {
          first: books.at(0)?.metadata.title,
          rest: books.length - 1,
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

    const promises = books
      .filter((b) => b.oneshot)
      .map(async (b) => {
        const s = await getParentSeries(b)
        return s ? [b.id, s] : null
      })
    const results = await Promise.all(promises)
    const oneshotSeries = new Map<string, SeriesDto>(
      results.filter((entry): entry is [string, SeriesDto] => entry !== null),
    )

    const entities = books.map((it) => ({ entity: it, extra: oneshotSeries.get(it.id)?.metadata }))

    dialogConfirmEdit.value.record = {
      entities: entities,
      bulkUpdates: {
        tags: [],
        tagsLock: undefined,
        authors: [],
        authorsLock: undefined,
        notableEntities: [],
        notableEntitiesLock: undefined,
        // oneshot
        genres: [],
        genresLock: undefined,
        sharingLabels: [],
        sharingLabelsLock: undefined,
      },
      uploadQueue: [],
    } satisfies EntitiesUpdateBulk<BookDto, OneShotAttributesBulk, BulkUpdateBook>

    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(EditBooks),
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
        BookDto,
        OneShotAttributesBulk,
        BulkUpdateBook
      >

      // update books
      let updateCount = 0
      for (const b of books) {
        // upload new posters
        await updatePoster(b, pick(updatedData, 'uploadQueue'))

        if (b.oneshot) {
          const parentSeries = oneshotSeries.get(b.id)
          if (parentSeries) {
            const seriesUpdateDto = pick(
              seriesMetadataToUpdateDto(
                applyBulkUpdates(parentSeries.metadata, updatedData.bulkUpdates),
              ),
              ...BulkUpdateBookOneShotFields,
            )
            await mutateUpdateSeriesMetadata({
              seriesId: parentSeries.id,
              metadata: seriesUpdateDto,
            })
              .then(() => {
                updateCount++
              })
              .catch((error) => {
                messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
              })
          }
        }

        // build update DTO
        const bookUpdateDto = pick(
          applyBulkUpdates(b.metadata as BookMetadataUpdateDto, updatedData.bulkUpdates),
          ...BulkUpdateBookFields,
        )

        await mutateUpdateBookMetadata({
          bookId: b.id,
          metadata: bookUpdateDto,
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
            description: 'Snackbar notification shown upon successful bulk book metadata update',
            defaultMessage: '{count, plural, one {# book} other {# books} } updated',
            id: 'Rgl4jp',
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
