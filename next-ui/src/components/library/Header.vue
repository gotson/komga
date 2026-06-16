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
      @click.prevent="display.xs.value ? (bottomSheet = true) : undefined"
    />
    <LibraryMenu
      v-if="display.smAndUp.value"
      :activator="`#${id}`"
      :library="effectiveLibrary"
    />
    <LibraryMenuBottomSheet
      v-if="display.xs.value"
      v-model="bottomSheet"
      :library="effectiveLibrary"
    />
  </div>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useCurrentUser } from '@/colada/users'
import { useGetLibrariesById } from '@/composables/libraries'
import LibraryMenuBottomSheet from '@/components/library/menu/LibraryMenuBottomSheet.vue'
import { useDisplay } from 'vuetify/framework'

const {
  library,
  libraryId,
  link = false,
} = defineProps<{
  library?: components['schemas']['LibraryDto']
  libraryId?: string
  link?: boolean
}>()

const { isAdmin } = useCurrentUser()
const id = useId()
const display = useDisplay()

const bottomSheet = ref(false)

const { library: librarySingle } = useGetLibrariesById(() => libraryId ?? 'unknown')

const effectiveLibrary = computed(() => library ?? librarySingle.value)
</script>

<script lang="ts"></script>

<style scoped></style>
