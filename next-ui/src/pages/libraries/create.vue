<template>
  <div>
    <v-empty-state
      :title="
        $formatMessage({
          description: 'No libraries view: title',
          defaultMessage: 'Welcome to Komga',
          id: 'Rm52xk',
        })
      "
      :text="
        isAdmin
          ? $formatMessage({
              description: 'No libraries view: subtitle for admin',
              defaultMessage: 'Create a library to get started',
              id: '1+Ux6s',
            })
          : $formatMessage({
              description: 'No libraries view: subtitle for non-admin',
              defaultMessage: 'This server does not have any library yet',
              id: 'rm0c6Y',
            })
      "
    >
      <template #media>
        <v-img
          src="@/assets/logo.svg"
          height="250px"
          class="mb-4"
        />
      </template>

      <template
        v-if="isAdmin"
        #actions
      >
        <v-btn
          :text="
            $formatMessage({
              description: 'No libraries view: button',
              defaultMessage: 'Create library',
              id: 'bzn96A',
            })
          "
          @mouseenter="(event: Event) => (activator = event.currentTarget as Element)"
          @click="createLibrary"
        />
      </template>
    </v-empty-state>
  </div>
</template>

<script lang="ts" setup>
import { useCurrentUser } from '@/colada/users'
import { useCreateLibraryDialog } from '@/composables/library/useCreateLibraryDialog'
import { useLibraries } from '@/colada/libraries'

const router = useRouter()
const { isAdmin } = useCurrentUser()
const { activator, prepareDialog: createLibrary } = useCreateLibraryDialog()
const { noLibraries } = useLibraries()

watch(noLibraries, (newNoLibraries) => {
  if (!newNoLibraries) {
    void router.push({ name: '/' })
  }
})
</script>

<route lang="yaml">
meta:
  requiresRole: USER
</route>
