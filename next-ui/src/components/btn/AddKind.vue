<template>
  <div>
    <v-menu
      v-model="menuIsOpen"
      close-on-content-click
    >
      <template #activator="{ props: activatorProps }">
        <v-btn
          v-bind="activatorProps"
          prepend-icon="i-mdi:plus"
          rounded="lg"
          :text="
            $formatMessage({
              description: 'Metadata edition: add kind button',
              defaultMessage: 'Add kind',
              id: 'lFWPpj',
            })
          "
          border
        />
      </template>

      <v-list
        density="compact"
        min-width="200"
      >
        <v-list-item
          v-for="kind in otherKinds"
          :key="kind"
          :title="
            notableEntitiesKindsMessages?.[kind]
              ? $formatMessage(notableEntitiesKindsMessages?.[kind])
              : kind
          "
          @click="emit('addKind', kind)"
        />

        <v-divider
          v-if="otherKinds.length > 0"
          class="my-1"
        />

        <v-list-item
          :title="
            $formatMessage({
              description: 'Metadata edition: add kind menu - custom kind',
              defaultMessage: 'Custom kind...',
              id: 'sutqn3',
            })
          "
          prepend-icon="i-mdi:plus"
          class="text-primary"
          @click="openCustomKindDialog"
        />
      </v-list>
    </v-menu>

    <!-- Custom Kind Dialog -->
    <v-dialog
      v-model="dialogIsOpen"
      max-width="400"
    >
      <v-card
        :title="
          $formatMessage({
            description: 'Metadata edition: add kind dialog title',
            defaultMessage: 'Add Custom Kind',
            id: 'AMQANB',
          })
        "
      >
        <v-card-text>
          <v-text-field
            v-model="customKindInput"
            :label="
              $formatMessage({
                description: 'Metadata edition: add kind dialog - input field label',
                defaultMessage: 'Kind name',
                id: 'bWiqyw',
              })
            "
            autofocus
            hide-details
            @keyup.enter="confirmCustomKind"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            :text="
              $formatMessage({
                description: 'Metadata edition: add kind dialog - cancel button',
                defaultMessage: 'Cancel',
                id: '8LT9D4',
              })
            "
            variant="plain"
            @click="dialogIsOpen = false"
          />
          <v-btn
            :text="
              $formatMessage({
                description: 'Metadata edition: add kind dialog - add button',
                defaultMessage: 'Add',
                id: 'J8l62K',
              })
            "
            color="primary"
            variant="flat"
            :disabled="!customKindInput.trim()"
            @click="confirmCustomKind"
          />
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { notableEntitiesKindsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { useArrayDifference } from '@vueuse/core'
import {
  DefaultNotableEntitiesKinds,
  notableEntitiesKindsMessages,
} from '@/types/referential/notableEntity'

const { excludeKinds = [] } = defineProps<{
  excludeKinds?: string[]
}>()

const emit = defineEmits<{
  addKind: [newKind: string]
}>()

const { data: apiKinds } = useQuery(() =>
  notableEntitiesKindsQuery({
    pageRequest: PageRequest.Unpaged(),
  }),
)
const definedKinds = computed(() =>
  Array.from(new Set([...(apiKinds.value?.content ?? []), ...DefaultNotableEntitiesKinds])),
)

const menuIsOpen = ref<boolean>(false)
const dialogIsOpen = ref<boolean>(false)
const customKindInput = ref<string>('')

// additional kinds present in the server but not on the book
const otherKinds = useArrayDifference(
  () => definedKinds.value,
  () => excludeKinds,
)

function openCustomKindDialog() {
  menuIsOpen.value = false
  customKindInput.value = ''
  dialogIsOpen.value = true
}

function confirmCustomKind() {
  const trimmed = customKindInput.value.trim()
  if (trimmed) {
    emit('addKind', trimmed)
  }
  dialogIsOpen.value = false
}
</script>
