import { useAddBookPoster, useDeleteBookPoster, useMarkBookPosterSelected } from '@/colada/books'
import {
  useAddSeriesPoster,
  useDeleteSeriesPoster,
  useMarkSeriesPosterSelected,
} from '@/colada/series'
import {
  useAddCollectionPoster,
  useDeleteCollectionPoster,
  useMarkCollectionPosterSelected,
} from '@/colada/collections'
import {
  useAddReadListPoster,
  useDeleteReadListPoster,
  useMarkReadListPosterSelected,
} from '@/colada/readlists'
import { type EntityDto, isBook, isCollection, isReadList, isSeries } from '@/functions/entity'
import type { PosterUpdate } from '@/types/entityUpdate'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useMessagesStore } from '@/stores/messages'

type PosterAddParams = {
  file: File
  selected: boolean
}

type PosterMarkSelectedParams = {
  thumbnailId: string
}

type PosterDeleteParams = {
  thumbnailId: string
}

type PosterAdapter = {
  addPoster: (params: PosterAddParams) => Promise<unknown>
  markSelected: (params: PosterMarkSelectedParams) => Promise<unknown>
  deletePoster: (params: PosterDeleteParams) => Promise<unknown>
}

export function usePosterUpdate() {
  const messagesStore = useMessagesStore()
  // book
  const { mutateAsync: mutateBookAddPoster } = useAddBookPoster()
  const { mutateAsync: mutateBookMarkPosterSelected } = useMarkBookPosterSelected()
  const { mutateAsync: mutateBookDeletePoster } = useDeleteBookPoster()
  // series
  const { mutateAsync: mutateSeriesAddPoster } = useAddSeriesPoster()
  const { mutateAsync: mutateSeriesMarkPosterSelected } = useMarkSeriesPosterSelected()
  const { mutateAsync: mutateSeriesDeletePoster } = useDeleteSeriesPoster()
  // collection
  const { mutateAsync: mutateCollectionAddPoster } = useAddCollectionPoster()
  const { mutateAsync: mutateCollectionMarkPosterSelected } = useMarkCollectionPosterSelected()
  const { mutateAsync: mutateCollectionDeletePoster } = useDeleteCollectionPoster()
  // read list
  const { mutateAsync: mutateReadListAddPoster } = useAddReadListPoster()
  const { mutateAsync: mutateReadListMarkPosterSelected } = useMarkReadListPosterSelected()
  const { mutateAsync: mutateReadListDeletePoster } = useDeleteReadListPoster()

  function getAdapter(entity: EntityDto): PosterAdapter {
    if (isBook(entity)) {
      return {
        addPoster: ({ file, selected }) =>
          mutateBookAddPoster({ bookId: entity.id, file, selected }),
        markSelected: ({ thumbnailId }) =>
          mutateBookMarkPosterSelected({ bookId: entity.id, thumbnailId }),
        deletePoster: ({ thumbnailId }) =>
          mutateBookDeletePoster({ bookId: entity.id, thumbnailId }),
      }
    }

    if (isSeries(entity)) {
      return {
        addPoster: ({ file, selected }) =>
          mutateSeriesAddPoster({ seriesId: entity.id, file, selected }),
        markSelected: ({ thumbnailId }) =>
          mutateSeriesMarkPosterSelected({ seriesId: entity.id, thumbnailId }),
        deletePoster: ({ thumbnailId }) =>
          mutateSeriesDeletePoster({ seriesId: entity.id, thumbnailId }),
      }
    }

    if (isCollection(entity)) {
      return {
        addPoster: ({ file, selected }) =>
          mutateCollectionAddPoster({ collectionId: entity.id, file, selected }),
        markSelected: ({ thumbnailId }) =>
          mutateCollectionMarkPosterSelected({ collectionId: entity.id, thumbnailId }),
        deletePoster: ({ thumbnailId }) =>
          mutateCollectionDeletePoster({ collectionId: entity.id, thumbnailId }),
      }
    }

    if (isReadList(entity)) {
      return {
        addPoster: ({ file, selected }) =>
          mutateReadListAddPoster({ readListId: entity.id, file, selected }),
        markSelected: ({ thumbnailId }) =>
          mutateReadListMarkPosterSelected({ readListId: entity.id, thumbnailId }),
        deletePoster: ({ thumbnailId }) =>
          mutateReadListDeletePoster({ readListId: entity.id, thumbnailId }),
      }
    }

    throw new Error('Unsupported entity type for poster updates')
  }

  async function updatePoster(entity: EntityDto, posters: Partial<PosterUpdate>) {
    const adapter = getAdapter(entity)

    // upload new posters
    if (posters.uploadQueue && posters.uploadQueue.length > 0) {
      for (const newPoster of posters.uploadQueue) {
        await adapter
          .addPoster({
            file: newPoster.file,
            selected: newPoster.selected,
          })
          .catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
      }
    }

    // mark existing poster as selected
    if (posters.selected) {
      await adapter
        .markSelected({
          thumbnailId: posters.selected.id,
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        })
    }

    // delete posters
    if (posters.deleteQueue && posters.deleteQueue.length > 0) {
      for (const posterToDelete of posters.deleteQueue) {
        await adapter
          .deletePoster({
            thumbnailId: posterToDelete.id,
          })
          .catch((error) => {
            messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          })
      }
    }
  }

  return {
    updatePoster,
  }
}
