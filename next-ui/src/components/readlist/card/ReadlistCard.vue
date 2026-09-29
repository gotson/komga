<template>
  <ItemCard
    :id="id"
    :title="title"
    :lines="lines"
    :poster-url="readListPosterUrl(readList.id, cacheStore.getVersion(readList.id))"
    :fab-icon="hasRole('PAGE_STREAMING') ? 'i-mdi:play' : undefined"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    :card-to="linkTo"
    v-bind="props"
    :disable-selection="!isAdmin"
    @selection="(val, event) => emit('selection', val, event)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="isAdmin || hasRole('FILE_DOWNLOAD') ? (bottomSheet = true) : undefined"
    @click-fab="openReader"
  />
  <ReadlistMenuSheet
    v-model="bottomSheet"
    :read-list="readList"
    :activator="menuActivator"
    add-select-action
  />
</template>

<script setup lang="ts">
import { readListPosterUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'
import { useEditReadListDialog } from '@/composables/readlist/useEditReadListDialog'
import type { ReadListDto } from '@/generated/openapi'
import { useImageCacheStore } from '@/stores/image-cache'
import { useBooks } from '@/composables/book/useBooks'
import { BrowsingContextKey, formatBrowsingContextAsQueryParam } from '@/functions/browsing-context'
import type { RouteLocationObject } from '@/types/route'

const intl = useIntl()
const cacheStore = useImageCacheStore()

const id = useId()

const { readList, ...props } = defineProps<
  {
    readList: ReadListDto
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

const title = computed<ItemCardTitle>(() => ({
  text: readList.name,
  lines: 2,
  routerLink: linkTo.value,
}))

const lines = computed<ItemCardLine[]>(() => [
  {
    text: intl.formatMessage(
      {
        description: 'Readlist card subtitle: count of books',
        defaultMessage: `{count, plural,
one {# book}
other {# books}
}`,
        id: 'WLfi1+',
      },
      { count: readList.bookIds.length },
    ),
  },
])

const context = inject(BrowsingContextKey, undefined)
const linkTo = computed<RouteLocationObject>(() => ({
  name: '/readlist/[id]',
  params: { id: readList.id },
  query: formatBrowsingContextAsQueryParam(toValue(context)),
}))

const { isAdmin, hasRole } = useCurrentUser()
const quickActionIcon = computed(() => (isAdmin.value ? 'i-mdi:pencil' : undefined))
const quickActionProps = computed(() => ({
  id: `${id}_quick`,
  onmouseenter: () => (editActivator.value = `#${id}_quick`),
}))
const menuIcon = computed(() =>
  isAdmin.value || hasRole('FILE_DOWNLOAD') ? 'i-mdi:dots-vertical' : undefined,
)
const menuProps = computed(() => ({
  onmouseenter: (event: Event) => (menuActivator.value = event.currentTarget as Element),
}))

const {
  prepareDialog: prepareEditDialog,
  showDialog: showEditDialog,
  activator: editActivator,
} = useEditReadListDialog()

function showEditMetadataDialog() {
  prepareEditDialog(readList)
  showEditDialog()
}

const menuActivator = ref()

const { readFirstBook } = useBooks(() => readList)
function openReader() {
  void readFirstBook()
}
</script>
