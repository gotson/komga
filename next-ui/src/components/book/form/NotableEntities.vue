<template>
  <div>
    <v-toolbar color="surface">
      <v-spacer />

      <BtnAddKind
        :exclude-kinds="bookKinds"
        class="me-2"
        @add-kind="(kind) => addKind(kind)"
      />

      <LockIcon v-model="model.metadata.notableEntitiesLock" />
    </v-toolbar>

    <v-container fluid>
      <v-row
        v-for="kind in bookKinds"
        :key="kind"
      >
        <v-col>
          <ComboboxNotableEntity
            v-if="kindGroups[kind]"
            v-model="kindGroups[kind]!.value"
            :kind="kind"
          />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { useQuery } from '@pinia/colada'
import { notableEntitiesKindsQuery } from '@/colada/referential'
import { PageRequest } from '@/types/PageRequest'
import type { BookDto } from '@/generated/openapi'
import { useLockWatcher } from '@/composables/form'

const model = defineModel<BookDto>({ required: true })
useLockWatcher(model.value.metadata, ['notableEntities'])

const { data: allKinds } = useQuery(() =>
  notableEntitiesKindsQuery({
    pageRequest: PageRequest.Unpaged(),
  }),
)

const customKinds = ref<string[]>([])

// copy on init, so the display order won't move when we add new kinds
const initialKinds = model.value.metadata.notableEntities.map((it) => it.kind)

// kinds from metadata, plus any custom kind defined in this form
const bookKinds = computed(() => Array.from(new Set([...initialKinds, ...customKinds.value])))

// contains all the kinds returned by the API, and the ones being added in this form
const definedKinds = computed(() =>
  Array.from(new Set([...(allKinds.value?.content ?? []), ...bookKinds.value])),
)

// helper to create a writable computed property for a given kind
function createKindComputed(kindName: string) {
  return computed<string[]>({
    get() {
      return model.value.metadata.notableEntities
        .filter((it) => it.kind === kindName)
        .map((it) => it.name)
    },
    set(updatedGroup: string[]) {
      const others = model.value.metadata.notableEntities.filter((it) => it.kind !== kindName)
      model.value.metadata.notableEntities = [
        ...others,
        ...updatedGroup.map((it) => ({ kind: kindName, name: it })),
      ]
    },
  })
}

// map each dynamic kind to its corresponding writable computed ref
const kindGroups = computed<Record<string, ReturnType<typeof createKindComputed>>>(() => {
  const map: Record<string, ReturnType<typeof createKindComputed>> = {}
  for (const kind of definedKinds.value) {
    map[kind] = createKindComputed(kind)
  }
  return map
})

function addKind(newKind: string) {
  if (newKind && !bookKinds.value.includes(newKind)) {
    customKinds.value.push(newKind)
  }
}
</script>
