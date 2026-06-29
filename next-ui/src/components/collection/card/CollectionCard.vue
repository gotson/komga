<template>
  <ItemCard
    :id="id"
    :title="title"
    :lines="lines"
    :poster-url="collectionPosterUrl(collection.id)"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    :card-to="`/collection/${collection.id}`"
    v-bind="props"
    :disable-selection="!isAdmin"
    @selection="(val, event) => emit('selection', val, event)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="isAdmin ? (bottomSheet = true) : undefined"
  />
  <CollectionMenuSheet
    v-model="bottomSheet"
    :collection="collection"
    :activator="menuActivator"
  />
</template>

<script setup lang="ts">
import { collectionPosterUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'
import { useEditCollectionDialog } from '@/composables/collection/useEditCollectionDialog'
import type { CollectionDto } from '@/generated/openapi'

const intl = useIntl()

const id = useId()

const { collection, ...props } = defineProps<
  {
    collection: CollectionDto
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

const title = computed<ItemCardTitle>(() => ({
  text: collection.name,
  lines: 2,
  routerLink: `/collection/${collection.id}`,
}))

const lines = computed<ItemCardLine[]>(() => [
  {
    text: intl.formatMessage(
      {
        description: 'Collection card subtitle: count of series',
        defaultMessage: `{count, plural,
one {# series}
other {# series}
}`,
        id: '0J3Gvp',
      },
      { count: collection.seriesIds.length },
    ),
  },
])

const { isAdmin } = useCurrentUser()
const quickActionIcon = computed(() => (isAdmin.value ? 'i-mdi:pencil' : undefined))
const quickActionProps = computed(() => ({
  id: `${id}_quick`,
  onmouseenter: () => (editActivator.value = `#${id}_quick`),
}))
const menuIcon = computed(() => (isAdmin.value ? 'i-mdi:dots-vertical' : undefined))
const menuProps = computed(() => ({
  onmouseenter: (event: Event) => (menuActivator.value = event.currentTarget as Element),
}))

const {
  prepareDialog: prepareEditCollectionDialog,
  showDialog: showEditCollectionDialog,
  activator: editActivator,
} = useEditCollectionDialog()

function showEditMetadataDialog() {
  prepareEditCollectionDialog(collection)
  showEditCollectionDialog()
}

const menuActivator = ref()
</script>
