<template>
  <ItemMenu
    :actions="actionsManagement"
    :activator="activator"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useCollectionActions } from '@/composables/collection/useCollectionActions'
import { type CollectionAction, collectionActionGroups } from '@/types/collection'
import { createOrderCompareFn } from '@/functions/sort'

const { collection, excludeActions = [] } = defineProps<{
  activator: string | Element
  collection: components['schemas']['CollectionDto']
  excludeActions?: CollectionAction[]
}>()

const { actions } = useCollectionActions(() => collection)

const actionsManagement = computed(() =>
  actions.value
    .filter(
      (it) =>
        collectionActionGroups.management.includes(it.action) &&
        !excludeActions.includes(it.action),
    )
    .toSorted(
      createOrderCompareFn(collectionActionGroups.management, (it) => it.action.toString()),
    ),
)
</script>

<script lang="ts"></script>

<style scoped></style>
