<template>
  <div>
    <v-toolbar color="surface">
      <v-spacer />

      <BtnAddRole
        :exclude-roles="bookRoles"
        class="me-2"
        @add-role="(role) => addRole(role)"
      />

      <LockIcon v-model="model.metadata.authorsLock" />
    </v-toolbar>

    <v-container fluid>
      <v-row
        v-for="role in bookRoles"
        :key="role"
      >
        <v-col>
          <ComboboxContributor
            v-if="roleGroups[role]"
            v-model="roleGroups[role]!.value"
            :role="role"
          />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { authorsRolesQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import type { BookDto } from '@/generated/openapi'
import { useLockWatcher } from '@/composables/form'

const model = defineModel<BookDto>({ required: true })
useLockWatcher(model.value.metadata, ['authors'])

const { data: allRoles } = useQuery(() =>
  authorsRolesQuery({
    pageRequest: PageRequest.Unpaged(),
  }),
)

const customRoles = ref<string[]>([])

// copy on init, so the display order won't move when we add new roles
const initialRoles = model.value.metadata.authors.map((it) => it.role)

// roles from metadata, plus any custom role defined in this form
const bookRoles = computed(() => Array.from(new Set([...initialRoles, ...customRoles.value])))

// contains all the roles returned by the API, and the ones being added in this form
const definedRoles = computed(() =>
  Array.from(new Set([...(allRoles.value?.content ?? []), ...bookRoles.value])),
)

// helper to create a writable computed property for a given role
function createRoleComputed(roleName: string) {
  return computed<string[]>({
    get() {
      return model.value.metadata.authors.filter((it) => it.role === roleName).map((it) => it.name)
    },
    set(updatedGroup: string[]) {
      const others = model.value.metadata.authors.filter((it) => it.role !== roleName)
      model.value.metadata.authors = [
        ...others,
        ...updatedGroup.map((it) => ({ role: roleName, name: it })),
      ]
    },
  })
}

// map each dynamic role to its corresponding writable computed ref
const roleGroups = computed<Record<string, ReturnType<typeof createRoleComputed>>>(() => {
  const map: Record<string, ReturnType<typeof createRoleComputed>> = {}
  for (const role of definedRoles.value) {
    map[role] = createRoleComputed(role)
  }
  return map
})

function addRole(newRole: string) {
  if (newRole && !bookRoles.value.includes(newRole)) {
    customRoles.value.push(newRole)
  }
}
</script>
