<template>
  <div>
    <v-toolbar color="surface">
      <v-spacer />

      <BtnAddKind
        :exclude-kinds="bookKinds"
        class="me-2"
        @add-kind="(kind) => addKind(kind)"
      />

      <LockIconBulk
        v-model="model.notableEntitiesLock"
        :initial-counts="initialCounts.notableEntitiesLock"
        :total-count="entities.length"
      />
    </v-toolbar>

    <v-container fluid>
      <v-row
        v-for="kind in bookKinds"
        :key="kind"
      >
        <v-col>
          <ComboboxInfiniteBulkNotableEntity
            v-if="kindGroups[kind]"
            v-model="kindGroups[kind]!.value"
            :initial-counts="getInitialCountByKind(kind)"
            :total-count="entities.length"
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
import type { BookMetadataDto } from '@/generated/openapi'
import type { BulkUpdateBook } from '@/types/entityUpdate'
import { getInitialCountsForKeys } from '@/functions/bulkEdit'
import type { BulkItemMultiField, InitialCountItem } from '@/types/bulk'

type BookBulkUpdateNotableEntities = Pick<BulkUpdateBook, 'notableEntities' | 'notableEntitiesLock'>

const model = defineModel<BookBulkUpdateNotableEntities>({ required: true })

const props = defineProps<{
  entities: Pick<BookMetadataDto, 'notableEntities' | 'notableEntitiesLock'>[]
}>()

const initialCounts = getInitialCountsForKeys(props.entities, [
  'notableEntities',
  'notableEntitiesLock',
])

function getInitialCountByKind(kind: string): InitialCountItem<string>[] {
  return initialCounts.notableEntities
    .filter((it) => it.value.kind === kind)
    .map((it) => ({ count: it.count, value: it.value.name }))
}

const { data: allKinds } = useQuery(() =>
  notableEntitiesKindsQuery({
    pageRequest: PageRequest.Unpaged(),
  }),
)

const customKinds = ref<string[]>([])

// copy on init, so the display order won't move when we add new kinds
const initialKinds = props.entities.flatMap((e) => e.notableEntities.map((it) => it.kind))

// kinds from metadata, plus any custom kind defined in this form
const bookKinds = computed(() => Array.from(new Set([...initialKinds, ...customKinds.value])))

// contains all the kinds returned by the API, and the ones being added in this form
const definedKinds = computed(() =>
  Array.from(new Set([...(allKinds.value?.content ?? []), ...bookKinds.value])),
)

// helper to create a writable computed property for a given kind
function createKindComputed(kindName: string) {
  return computed<BulkItemMultiField<string>[]>({
    get() {
      return (
        model.value.notableEntities
          ?.filter((it) => it.value.kind === kindName)
          .map((it) => ({ ...it, value: it.value.name })) || []
      )
    },
    set(updatedGroup: BulkItemMultiField<string>[]) {
      const others = model.value.notableEntities?.filter((it) => it.value.kind !== kindName) || []
      model.value.notableEntities = [
        ...others,
        ...updatedGroup.map((it) => ({ ...it, value: { kind: kindName, name: it.value } })),
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
