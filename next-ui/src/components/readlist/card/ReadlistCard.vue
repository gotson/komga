<template>
  <ItemCard
    :id="id"
    :title="title"
    :lines="lines"
    :poster-url="readListPosterUrl(readList.id)"
    :quick-action-icon="quickActionIcon"
    :quick-action-props="quickActionProps"
    :menu-icon="menuIcon"
    :menu-props="menuProps"
    v-bind="props"
    @selection="(val, event) => emit('selection', val, event)"
    @click-quick-action="showEditMetadataDialog()"
    @card-long-press="bottomSheet = true"
  />
  <ReadlistMenu
    :read-list="readList"
    :activator="menuActivator"
  />
  <ReadlistMenuBottomSheet
    v-model="bottomSheet"
    :read-list="readList"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { readListPosterUrl } from '@/api/images'
import { useIntl } from 'vue-intl'
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { useCurrentUser } from '@/colada/users'
import ReadlistMenu from '@/components/readlist/menu/ReadlistMenu.vue'
import ReadlistMenuBottomSheet from '@/components/readlist/menu/ReadlistMenuBottomSheet.vue'
import { useEditReadListDialog } from '@/composables/readlist/useEditReadListDialog'

const intl = useIntl()

const id = useId()

const { readList, ...props } = defineProps<
  {
    readList: components['schemas']['ReadListDto']
  } & ItemCardProps
>()
const emit = defineEmits<ItemCardEmits>()

const bottomSheet = ref(false)

const title = computed<ItemCardTitle>(() => ({
  text: readList.name,
  lines: 2,
  routerLink: `/readlist/${readList.id}`,
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
  prepareDialog: prepareEditDialog,
  showDialog: showEditDialog,
  activator: editActivator,
} = useEditReadListDialog()

function showEditMetadataDialog() {
  prepareEditDialog(readList)
  showEditDialog()
}

const menuActivator = ref()
</script>
