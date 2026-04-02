<template>
  <div v-if="isAdmin">
    <v-icon-btn
      :id="id"
      icon="i-mdi:dots-vertical"
      variant="text"
      :aria-label="
        $formatMessage({
          description: 'Collection menu button: aria label',
          defaultMessage: 'collection menu',
          id: '1sYohL',
        })
      "
      @click.prevent
    />
    <CollectionMenu
      :activator="`#${id}`"
      :collection="collection"
    />
  </div>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useCurrentUser } from '@/colada/users'

const { collection } = defineProps<{
  collection: components['schemas']['CollectionDto']
}>()

const { isAdmin } = useCurrentUser()

const id = useId()
</script>

<script lang="ts"></script>

<style scoped></style>
