<template>
  <v-list-item
    :title="library.name"
    :to="`/libraries/${library.id}`"
    prepend-icon="blank"
  >
    <template
      v-if="library.unavailable"
      #subtitle
    >
      <span class="text-error">{{
        $formatMessage({
          description: 'Library list item subtitle: unavailable',
          defaultMessage: 'Unavailable',
          id: '5rziSG',
        })
      }}</span>
    </template>

    <template #append>
      <v-icon-btn
        v-if="isAdmin"
        :id="`${id}${library.id}`"
        icon="i-mdi:dots-vertical"
        variant="text"
        :aria-label="
          $formatMessage({
            description: 'Library menu button: aria label',
            defaultMessage: 'library menu',
            id: '3gimvl',
          })
        "
        @click.prevent="bottomSheet = true"
      />
      <LibraryMenuSheet
        v-model="bottomSheet"
        :activator="`#${id}${library.id}`"
        :library="library"
      />
    </template>
  </v-list-item>
</template>

<script setup lang="ts">
import { useCurrentUser } from '@/colada/users'
import type { LibraryDto } from '@/generated/openapi'

defineProps<{
  library: LibraryDto
}>()

const { isAdmin } = useCurrentUser()
const id = useId()

const bottomSheet = ref(false)
</script>
