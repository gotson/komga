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
      @click="showExtraActions()"
    />
  </div>
  <ReadlistMenu
    v-if="display.smAndUp.value"
    :read-list="readList"
    :activator="`#${id}`"
    :exclude-actions="excludeActions"
  />
  <ReadlistMenuBottomSheet
    v-if="display.xs.value"
    v-model="bottomSheet"
    :read-list="readList"
    :exclude-actions="excludeActions"
  />
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useDisplay } from 'vuetify/framework'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useReadListActions } from '@/composables/readlist/useReadListActions'
import { ReadListAction } from '@/types/readlist'

const props = defineProps<{
  readList: components['schemas']['ReadListDto']
}>()

const id = useId()
const display = useDisplay()
const { actions } = useReadListActions(() => props.readList)

const bottomSheet = ref(false)

const editAction = computed(() => actions.value.find((it) => it.action === ReadListAction.EDIT))
const excludeActions = [ReadListAction.EDIT]
const hasExtra = computed(() => actions.value.some((it) => !excludeActions.includes(it.action)))

function showExtraActions() {
  if (display.xs.value) bottomSheet.value = true
}
</script>
