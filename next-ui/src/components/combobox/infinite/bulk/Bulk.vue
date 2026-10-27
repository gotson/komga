<template>
  <v-autocomplete
    v-model="model"
    v-model:search="search"
    :items="autocompleteItems"
    item-title="text"
    item-value="text"
    :label="label"
    chips
    multiple
    hide-details="auto"
    :hide-no-data="false"
    no-auto-scroll
    return-object
    @keydown.enter="acceptAndClear"
    @keydown.esc="search = undefined"
  >
    <template #prepend>
      <slot name="prepend" />
    </template>

    <!-- Infinite scroll trigger at bottom of list -->
    <template
      v-if="!isSearching && hasMoreData"
      #append-item
    >
      <div
        v-intersect.quiet="
          (isIntersecting: boolean) => (isIntersecting ? emit('loadMore') : undefined)
        "
      ></div>
    </template>

    <template
      v-if="isSearching"
      #no-data
    >
      <v-list-item @click="acceptAndClear">
        <v-list-item-title class="text-wrap">
          <FormattedMessage
            :message-descriptor="message"
            :values="{ search: search }"
          >
            <template #kbd="Content">
              <kbd>
                <component :is="Content" />
              </kbd>
            </template>
            <template #b="Content">
              <strong>
                <component :is="Content" />
              </strong>
            </template>
          </FormattedMessage>
        </v-list-item-title>
      </v-list-item>
    </template>

    <!--  Override the item slot to intercept clicks and show indeterminate state on the checkbox  -->
    <template #item="{ props, item }">
      <v-list-item
        v-bind="omit(props, 'onClick')"
        :title="item.value"
        :active="isInModel(item) && item.state !== 'remove'"
        @click.prevent.stop="toggleFromList(item)"
      >
        <template #prepend>
          <v-checkbox-btn
            :model-value="listCheckboxState(item) === 'ticked'"
            :indeterminate="listCheckboxState(item) === 'indeterminate'"
            color="primary"
          />
        </template>
      </v-list-item>
    </template>

    <!--  Override the chip slot to display a multi-state chip  -->
    <template #chip="{ props, item }">
      <v-chip
        v-bind="{ ...props, ...getChipProps(item) }"
        :text="item.value"
        rounded
        @click.stop="cycleChipState(item)"
      >
        <template #prepend>
          <v-icon
            v-if="item.state === 'new'"
            start
            icon="i-mdi:plus-circle-outline"
            size="small"
          />
          <v-icon
            v-if="item.state === 'remove'"
            start
            icon="i-mdi:minus-circle-outline"
            size="small"
          />
        </template>

        <template #default>
          <span>{{ item.value }}</span>
          <span
            v-if="item.state === 'mixed'"
            class="chip-count-badge ms-1"
          >
            {{ item.count }}
          </span>
        </template>

        <template #append>
          <v-icon
            icon="i-mdi:close-circle"
            class="ms-1"
            @click.stop.prevent="handleChipClose(item)"
          />
        </template>
      </v-chip>
    </template>
  </v-autocomplete>
</template>

<script setup lang="ts">
import { defineMessage } from 'vue-intl'
import { omit } from '@/functions/object'
import type { BulkInputProps, BulkItemMultiField } from '@/types/bulk'

const model = defineModel<BulkItemMultiField<string>[]>({ default: () => [] })
const search = defineModel<string | undefined>('search', { default: '' })

const {
  items = [],
  hasMoreData = false,
  initialCounts,
  totalCount,
} = defineProps<
  {
    items?: string[]
    hasMoreData?: boolean
    label?: string
  } & BulkInputProps<string>
>()

const emit = defineEmits<{
  loadMore: []
}>()

const isSearching = computed(() => (search.value?.trim()?.length ?? 0) > 0)

// Track original counts for handling resets
const originalCounts = new Map<string, number>()

// Hydrate model from initialCounts if needed
if (initialCounts.length > 0 && model.value.length === 0) {
  const initializedModel: BulkItemMultiField<string>[] = []

  for (const item of initialCounts) {
    const value = item.value
    const count = item.count

    // Store original count for resets
    originalCounts.set(value, count)

    // Compute state based on count vs totalCount
    if (count >= totalCount) {
      initializedModel.push({ value: value, state: 'all' })
    } else {
      initializedModel.push({ value: value, state: 'mixed', count })
    }
  }

  model.value = initializedModel
}

// Deduplicate autocomplete suggestions against existing model chips by `value`
const autocompleteItems = computed<BulkItemMultiField<string>[]>(() => {
  const modelMap = new Map(model.value.map((m) => [m.value, m]))

  const suggestions: BulkItemMultiField<string>[] = items.map((it) => {
    return modelMap.get(it) ?? { value: it, state: 'new' }
  })

  // Ensure items currently in `model` remain available in the autocomplete pool
  for (const modelItem of model.value) {
    if (!suggestions.some((it) => it.value === modelItem.value)) {
      suggestions.push(modelItem)
    }
  }

  return suggestions
})

function acceptAndClear() {
  const trimmed = search.value?.trim()
  if (trimmed) {
    const existing = model.value.find((it) => it.value === trimmed)
    if (!existing) model.value = [...model.value, { value: trimmed, state: 'new' }]
    else cycleChipState(existing)
  }
  search.value = undefined
}

/**
 * State Machine Cycle on Chip Click.
 *
 * | Initial State                    | Click Chip Action (Cycle)                                                                                               | Click Close (X) Action                                                                            |
 * |:---------------------------------|:------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------|
 * | **`mixed`** *(partial)*          | Transitions to **`new`**                                                                                                | Transitions directly to **`remove`**                                                              |
 * | **`all`** *(100% present)*       | Transitions to **`remove`**                                                                                             | Transitions directly to **`remove`**                                                              |
 * | **`new`** *(added in session)*   | **Pre-existing item**: Transitions to **`remove`**. **Draft item**: Removed from model completely                       | **Pre-existing item**: Transitions to **`remove`**. **Draft item**: Removed from model completely |
 * | **`remove`** *(marked deletion)* | **If `initialCount == totalCount`**: Restores to **`all`**. **If `initialCount < totalCount`**: Restores to **`mixed`** | Restores to original state (**`all`** or **`mixed`**)                                             |
 *
 */
function cycleChipState(target: BulkItemMultiField<string>) {
  model.value = model.value.flatMap((item) => {
    if (item.value !== target.value) return [item]

    const initialCount = originalCounts.get(item.value)
    const isPreExisting = initialCount !== undefined

    switch (item.state) {
      case 'mixed':
        // Transitioning to 'new': count is discarded
        return [
          {
            value: item.value,
            state: 'new',
          },
        ]

      case 'new':
        if (isPreExisting) {
          return [
            {
              value: item.value,
              state: 'remove',
            },
          ]
        } else {
          return [] // Created in this session -> drop completely
        }

      case 'all':
        return [
          {
            value: item.value,
            state: 'remove',
          },
        ]

      case 'remove':
        if (isPreExisting) {
          if (initialCount === totalCount) {
            return [
              {
                value: item.value,
                state: 'all',
              },
            ]
          } else {
            // Re-entering 'mixed': count is strictly required here
            return [
              {
                value: item.value,
                state: 'mixed',
                count: initialCount,
              },
            ]
          }
        } else {
          return []
        }
    }
  })
}

// Handling explicit (X) close click
function handleChipClose(target: BulkItemMultiField<string>) {
  const isPreExisting = originalCounts.has(target.value)

  if (target.state === 'new' && !isPreExisting) {
    // Draft item created in this session -> drop completely from model
    model.value = model.value.filter((i) => i.value !== target.value)
  } else if (target.state === 'remove') {
    // If it's ALREADY in 'remove' state and user clicks (X) again, reset it to original state
    cycleChipState(target)
  } else {
    // For 'all' or 'mixed', clicking (X) directly forces it into 'remove' state
    model.value = model.value.map((item) => {
      if (!item || item.value !== target.value) return item
      return {
        value: item.value,
        state: 'remove',
      } satisfies BulkItemMultiField<string>
    })
  }
}

function toggleFromList(target: BulkItemMultiField<string>) {
  const existing = model.value.find((it) => it.value === target.value)

  if (!existing) {
    // item doesn't exist yet, add as 'new'
    model.value = [...model.value, target]
  } else {
    cycleChipState(target)
  }
}

function getChipProps(element: BulkItemMultiField<string>) {
  switch (element.state) {
    case 'all':
      return {
        color: 'primary',
        variant: 'flat' as const,
      }
    case 'mixed':
      return {
        color: 'primary',
        variant: 'outlined' as const,
      }
    case 'new':
      return {
        color: 'success',
        variant: 'flat' as const,
      }
    case 'remove':
      return {
        color: 'error',
        variant: 'outlined' as const,
        class: 'text-decoration-line-through',
      }
  }
}

function listCheckboxState(
  item: BulkItemMultiField<string>,
): 'ticked' | 'indeterminate' | 'unticked' {
  switch (item.state) {
    case 'all':
      return 'ticked'
    case 'new':
      return isInModel(item) ? 'ticked' : 'unticked'
    case 'remove':
      return 'unticked'
    case 'mixed':
      return 'indeterminate'
  }
}

const modelValueSet = computed(() => new Set(model.value.map((it) => it.value)))
function isInModel(item: BulkItemMultiField<string>): boolean {
  return modelValueSet.value.has(item.value)
}

const message = defineMessage({
  description: 'Combobox: no result matching search',
  defaultMessage:
    'No results matching "<b>{search}</b>". Press <kbd>enter</kbd> or click to create a new one.',
  id: 'iCYg8R',
})
</script>

<style scoped lang="scss">
.chip-count-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 0.6rem;
  min-width: 1.125rem;
  height: 1.125rem;
  padding: 0 0.25rem;
  border-radius: 9999px;
  background-color: rgba(var(--v-theme-on-surface), 0.12);
  color: rgba(var(--v-theme-on-surface), 0.87);
}
</style>
