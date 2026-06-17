<template>
  <div class="d-flex ga-2">
    <v-btn
      v-if="editAction"
      :prepend-icon="editAction.icon"
      :text="editAction.title"
      :disabled="editAction.disabled"
      @click="editAction.onClick?.()"
      @mouseenter="editAction?.onMouseenter?.($event)"
    />

    <v-icon-btn
      v-if="hasExtra"
      :id="id"
      v-tooltip:bottom="$formatMessage(commonMessages.buttonMore)"
      icon="i-mdi:dots-horizontal"
      @click="bottomSheet = true"
    />
  </div>
  <CollectionMenuSheet
    v-model="bottomSheet"
    :collection="collection"
    :activator="`#${id}`"
    :exclude-actions="excludeActions"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useCollectionActions } from '@/composables/collection/useCollectionActions'
import { CollectionAction } from '@/types/action/collection'

const props = defineProps<{
  collection: components['schemas']['CollectionDto']
}>()

const id = useId()
const { actions } = useCollectionActions(() => props.collection)

const bottomSheet = ref(false)

const editAction = computed(() => actions.value.find((it) => it.action === CollectionAction.EDIT))
const excludeActions = [CollectionAction.EDIT]
const hasExtra = computed(() => actions.value.some((it) => !excludeActions.includes(it.action)))
</script>
