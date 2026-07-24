<template>
  <div
    v-if="effectiveLibrary"
    class="d-flex ga-2 align-center"
  >
    <div class="text-title-large">
      <RouterLink
        v-if="link"
        class="link-underline"
        :to="`/libraries/${effectiveLibrary.id}`"
        >{{ effectiveLibrary.name }}
      </RouterLink>
      <span v-else>{{ effectiveLibrary.name }}</span>
    </div>
    <v-icon-btn
      v-if="isAdmin"
      :id="id"
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
      :activator="`#${id}`"
      :library="effectiveLibrary"
    />
  </div>
</template>

<script setup lang="ts">
import { useCurrentUser } from '@/colada/users'
import { useGetLibrariesByViewId } from '@/composables/libraries'
import type { LibraryDto } from '@/generated/openapi'

const {
  library,
  libraryId,
  link = false,
} = defineProps<{
  library?: LibraryDto
  libraryId?: string
  link?: boolean
}>()

const { isAdmin } = useCurrentUser()
const id = useId()

const bottomSheet = ref(false)

const { library: librarySingle } = useGetLibrariesByViewId(() => libraryId ?? 'unknown')

const effectiveLibrary = computed(() => library ?? librarySingle.value)
</script>

<script lang="ts"></script>

<style scoped></style>
