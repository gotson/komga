import { storeToRefs } from 'pinia'
import { type DialogResult, useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'
import EditBook from '@/components/book/form/Edit.vue'
import EditSeries from '@/components/series/form/Edit.vue'
import PrevNext from '@/components/item/form/PrevNext.vue'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useUpdateBookMetadata } from '@/colada/books'
import type {
  BookDto,
  BookMetadataUpdateDto,
  SeriesDto,
  SeriesMetadataDto,
} from '@/generated/openapi'
import { useUpdateSeriesMetadata } from '@/colada/series'
import { type OneShotAttributes, vOneShotAttributes } from '@/types/oneshot'
import { createEntityUpdate, type EntityUpdate } from '@/types/entityUpdate'
import { isBook, isSeries } from '@/functions/entity'
import { getFirstBookInParent } from '@/functions/book-container'
import { seriesMetadataToUpdateDto } from '@/functions/series'
import { pick, pickSchemaKeys } from '@/functions/object'
import { usePosterUpdate } from '@/composables/entity/usePosterUpdate'
import { getParentSeries } from '@/functions/book'

type SupportedEntity = BookDto | SeriesDto

export function useEditDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateUpdateBookMetadata } = useUpdateBookMetadata()
  const { mutateAsync: mutateUpdateSeriesMetadata } = useUpdateSeriesMetadata()
  const { updatePoster } = usePosterUpdate()

  const prepareDialog = (entities: SupportedEntity[], callback: () => void = () => {}) => {
    // use a ref so we can cycle entities that have been modified
    const toEdit = ref<SupportedEntity[]>([...entities])

    // display first entity
    void displayOne(toEdit.value.at(0)!, toEdit)

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

      const updatedData = dialogConfirmEdit.value.record as EntityUpdate<
        SupportedEntity,
        OneShotAttributes | undefined
      >
      // keep track in the ref
      toEdit.value = toEdit.value.map((it) =>
        it.id !== updatedData.entity.id ? it : updatedData.entity,
      )
      // update server
      await saveOne(updatedData)

      const entityIndex = toEdit.value.findIndex((it) => it.id === updatedData.entity.id)

      // if it's the last entity, close dialog
      if (entityIndex === toEdit.value.length - 1) {
        hideDialog()
        callback()
      } else {
        // display next entity
        await displayOne(toEdit.value.at(entityIndex + 1)!, toEdit)
        setLoading(false)
      }
    }
  }

  async function displayOne(entity: SupportedEntity, entities: Ref<SupportedEntity[]>) {
    const entityRef = ref(entity)
    const isMultiple = entities.value.length > 1
    const isLast = entities.value.at(-1)?.id === entity.id

    const kindIsBook = (isSeries(entity) && entity.oneshot) || isBook(entity)

    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage(
        kindIsBook
          ? {
              description: 'Edit book metadata dialog title',
              defaultMessage: 'Edit book metadata',
              id: 'mtUacw',
            }
          : {
              description: 'Edit series metadata dialog title',
              defaultMessage: 'Edit series metadata',
              id: '1bxWGd',
            },
      ),
      subtitle: entity.metadata.title,
      maxWidth: 900,
      cardTextProps: {
        class: 'px-0',
      },
      cancelText: isMultiple
        ? intl.formatMessage({
            description: 'Edit entity dialog: close button text',
            defaultMessage: 'Close',
            id: '+sXZtI',
          })
        : undefined,
      okText: isMultiple
        ? isLast
          ? intl.formatMessage({
              description: 'Edit entity dialog: confirmation button text for last entity',
              defaultMessage: 'Save and close',
              id: 'SF1C8o',
            })
          : intl.formatMessage({
              description:
                'Edit entity dialog: confirmation button text for all entities except last',
              defaultMessage: 'Save and next',
              id: 'nXEvYQ',
            })
        : undefined,
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }

    // load component dynamically depending on entity kind
    let component: Component
    if (kindIsBook) component = EditBook
    else if (isSeries(entity)) component = EditSeries

    dialogConfirmEdit.value.slots = {
      default: {
        component: markRaw(component!),
      },
      prependActions: isMultiple
        ? {
            component: markRaw(PrevNext),
            props: {
              items: entities,
              current: entityRef,
            },
            handlers: {
              previous: (item: SupportedEntity) => displayOne(item, entities),
              next: (item: SupportedEntity) => displayOne(item, entities),
            },
          }
        : undefined,
    }

    // create record depending on entity kind
    if (isSeries(entity)) {
      // for series one-shot we need to get the corresponding book
      if (entity.oneshot) {
        const book = await getFirstBookInParent(entity, false)
        if (book) {
          // book is the main entity, we already have series metadata as the oneshot extra
          dialogConfirmEdit.value.record = createEntityUpdate(
            book,
            pickSchemaKeys(vOneShotAttributes, entity.metadata),
          )
          // we also need to update the shared ref for ID tracking
          const index = entities.value.findIndex((it) => it.id === entity.id)
          if (index >= 0) entities.value = entities.value.with(index, book)
          entityRef.value = book
        }
      } else {
        // normal series edit, no extra needed
        dialogConfirmEdit.value.record = createEntityUpdate(entity)
      }
    }

    if (isBook(entity)) {
      if (entity.oneshot) {
        const series = await getParentSeries(entity)
        if (series) {
          dialogConfirmEdit.value.record = createEntityUpdate(
            entity,
            pickSchemaKeys(vOneShotAttributes, series.metadata),
          )
        }
      } else {
        dialogConfirmEdit.value.record = createEntityUpdate(entity)
      }
    }
  }

  async function saveOne(
    updatedData: EntityUpdate<SupportedEntity, OneShotAttributes | undefined>,
  ) {
    // update posters
    await updatePoster(
      updatedData.entity,
      pick(updatedData, 'uploadQueue', 'selected', 'deleteQueue'),
    )

    if (isBook(updatedData.entity)) {
      // update series extra if book is oneshot
      if (updatedData.entity.oneshot && updatedData.extra) {
        await mutateUpdateSeriesMetadata({
          seriesId: updatedData.entity.seriesId,
          metadata: seriesMetadataToUpdateDto({
            ...updatedData.extra,
            title: updatedData.entity.metadata.title,
            titleLock: updatedData.entity.metadata.titleLock,
            status: 'ENDED',
          }),
        }).catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
      }

      // update book
      const updateDto = {
        ...updatedData.entity.metadata,
        ...(updatedData.entity.oneshot ? { number: '1', numberSort: 1 } : {}),
      } satisfies BookMetadataUpdateDto

      await mutateUpdateBookMetadata({ bookId: updatedData.entity.id, metadata: updateDto })
        .then(() => {
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful book metadata update',
                defaultMessage: 'Book metadata updated: {book}',
                id: 'P8Ox+D',
              },
              {
                book: updateDto.title,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
    } else if (isSeries(updatedData.entity)) {
      // update series
      const updateDto = {
        ...updatedData.entity.metadata,
        ...(updatedData.entity.oneshot ? { status: 'ENDED' } : {}),
      } satisfies SeriesMetadataDto

      await mutateUpdateSeriesMetadata({
        seriesId: updatedData.entity.id,
        metadata: seriesMetadataToUpdateDto(updateDto),
      })
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
