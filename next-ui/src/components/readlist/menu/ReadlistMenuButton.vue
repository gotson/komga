<template>
  <div v-if="isAdmin || hasRole(UserRoles.FILE_DOWNLOAD)">
    <v-icon-btn
      :id="id"
      icon="i-mdi:dots-vertical"
      variant="text"
      :aria-label="
        $formatMessage({
          description: 'Readlist menu button: aria label',
          defaultMessage: 'readlist menu',
          id: '1qZTv3',
        })
      "
      @click.prevent
    />
    <ReadlistMenu
      :activator="`#${id}`"
      :read-list="readList"
    />
  </div>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useCurrentUser } from '@/colada/users'
import { UserRoles } from '@/types/UserRoles'

const { readList } = defineProps<{
  readList: components['schemas']['ReadListDto']
}>()

const { isAdmin, hasRole } = useCurrentUser()

const id = useId()
</script>

<script lang="ts"></script>

<style scoped></style>
