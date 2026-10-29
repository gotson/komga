<template>
  <div>
    <v-toolbar color="surface">
      <v-spacer />

      <BtnAddRole
        :exclude-roles="bookRoles"
        class="me-2"
        @add-role="(role) => addRole(role)"
      />

      <LockIconBulk
        v-model="model.authorsLock"
        :initial-counts="initialCounts.authorsLock"
        :total-count="entities.length"
      />
    </v-toolbar>

    <v-container fluid>
      <v-row
        v-for="role in bookRoles"
        :key="role"
      >
        <v-col>
          <ComboboxInfiniteBulkContributor
            v-if="roleGroups[role]"
            v-model="roleGroups[role]!.value"
            :initial-counts="getInitialCountByRole(role)"
            :total-count="entities.length"
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
import type { BookMetadataDto } from '@/generated/openapi'
import type { BulkUpdateBook } from '@/types/entityUpdate'
import { getInitialCountsForKeys } from '@/functions/bulkEdit'
import type { BulkItemMultiField, InitialCountItem } from '@/types/bulk'

type BookBulkUpdateContributors = Pick<BulkUpdateBook, 'authors' | 'authorsLock'>

const model = defineModel<BookBulkUpdateContributors>({ required: true })

const props = defineProps<{
  entities: Pick<BookMetadataDto, 'authors' | 'authorsLock'>[]
}>()

const initialCounts = getInitialCountsForKeys(props.entities, ['authors', 'authorsLock'])

function getInitialCountByRole(role: string): InitialCountItem<string>[] {
  return initialCounts.authors
    .filter((it) => it.value.role === role)
    .map((it) => ({ count: it.count, value: it.value.name }))
}

const { data: allRoles } = useQuery(() =>
  authorsRolesQuery({
    pageRequest: PageRequest.Unpaged(),
  }),
)

const customRoles = ref<string[]>([])

// copy on init, so the display order won't move when we add new roles
const initialRoles = props.entities.flatMap((e) => e.authors.map((it) => it.role))

// roles from metadata, plus any custom role defined in this form
const bookRoles = computed(() => Array.from(new Set([...initialRoles, ...customRoles.value])))

// contains all the roles returned by the API, and the ones being added in this form
const definedRoles = computed(() =>
  Array.from(new Set([...(allRoles.value?.content ?? []), ...bookRoles.value])),
)

// helper to create a writable computed property for a given role
function createRoleComputed(roleName: string) {
  return computed<BulkItemMultiField<string>[]>({
    get() {
      return (
        model.value.authors
          ?.filter((it) => it.value.role === roleName)
          .map((it) => ({ ...it, value: it.value.name })) || []
      )
    },
    set(updatedGroup: BulkItemMultiField<string>[]) {
      const others = model.value.authors?.filter((it) => it.value.role !== roleName) || []
      model.value.authors = [
        ...others,
        ...updatedGroup.map((it) => ({ ...it, value: { role: roleName, name: it.value } })),
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
