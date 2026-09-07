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
              description: 'Metadata edition: add role button',
              defaultMessage: 'Add role',
              id: 'fRfbEb',
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
          v-for="role in otherRoles"
          :key="role"
          :title="
            contributorsRolesMessages?.[role]
              ? $formatMessage(contributorsRolesMessages?.[role])
              : role
          "
          @click="emit('addRole', role)"
        />

        <v-divider
          v-if="otherRoles.length > 0"
          class="my-1"
        />

        <v-list-item
          title="Custom role..."
          prepend-icon="i-mdi:plus"
          class="text-primary"
          @click="openCustomRoleDialog"
        />
      </v-list>
    </v-menu>

    <!-- Custom Role Dialog -->
    <v-dialog
      v-model="dialogIsOpen"
      max-width="400"
    >
      <v-card title="Add Custom Role">
        <v-card-text>
          <v-text-field
            v-model="customRoleInput"
            label="Role Name"
            autofocus
            hide-details
            @keyup.enter="confirmCustomRole"
          />
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn
            text="Cancel"
            variant="plain"
            @click="dialogIsOpen = false"
          />
          <v-btn
            text="Add"
            color="primary"
            variant="flat"
            :disabled="!customRoleInput.trim()"
            @click="confirmCustomRole"
          />
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { authorsRolesQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import { useArrayDifference } from '@vueuse/core'
import { contributorsRolesMessages, DefaultContributorRoles } from '@/types/referential'

const { excludeRoles = [] } = defineProps<{
  excludeRoles?: string[]
}>()

const emit = defineEmits<{
  addRole: [newRole: string]
}>()

const { data: apiRoles } = useQuery(() =>
  authorsRolesQuery({
    pageRequest: PageRequest.Unpaged(),
  }),
)
const definedRoles = computed(() =>
  Array.from(new Set([...(apiRoles.value?.content ?? []), ...DefaultContributorRoles])),
)

const menuIsOpen = ref<boolean>(false)
const dialogIsOpen = ref<boolean>(false)
const customRoleInput = ref<string>('')

// additional roles present in the server but not on the book
const otherRoles = useArrayDifference(
  () => definedRoles.value,
  () => excludeRoles,
)

function openCustomRoleDialog() {
  menuIsOpen.value = false
  customRoleInput.value = ''
  dialogIsOpen.value = true
}

function confirmCustomRole() {
  const trimmed = customRoleInput.value.trim()
  if (trimmed) {
    emit('addRole', trimmed)
  }
  dialogIsOpen.value = false
}
</script>
