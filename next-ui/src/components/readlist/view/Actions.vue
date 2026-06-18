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
  <ReadlistMenuSheet
    v-model="bottomSheet"
    :read-list="readList"
    :activator="`#${id}`"
    :exclude-actions="excludeActions"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useReadListActions } from '@/composables/readlist/useReadListActions'
import { ActionName } from '@/types/action/action'

const props = defineProps<{
  readList: components['schemas']['ReadListDto']
}>()

const id = useId()
const { actions } = useReadListActions(() => props.readList)

const bottomSheet = ref(false)

const editAction = computed(() =>
  actions.value.find((it) => it.action === ActionName.EDIT_READLIST),
)
const excludeActions = [ActionName.EDIT_READLIST]
const hasExtra = computed(() => actions.value.some((it) => !excludeActions.includes(it.action)))
</script>
