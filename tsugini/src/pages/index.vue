<template>
  <q-page class="row items-center justify-evenly">
    <example-component
      title="Example component"
      active
      :todos="todos"
      :meta="meta"
    ></example-component>
  </q-page>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { Todo, Meta } from 'components/models'

const todos = ref<Todo[]>(
  [...Array(100).keys()].map((i) => ({
    id: i + 1,
    content: `ct${i + 1}`,
  })),
)

const meta = ref<Meta>({
  totalCount: 1200,
})
</script>
