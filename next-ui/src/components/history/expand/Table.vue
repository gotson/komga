<template>
  <v-sheet
    rounded="lg"
    border
  >
    <v-table density="compact">
      <tbody
        v-for="row in rows"
        :key="row.header"
      >
        <tr>
          <th>{{ row.header }}</th>
          <td>{{ row.value }}</td>
        </tr>
      </tbody>
    </v-table>
  </v-sheet>
</template>

<script setup lang="ts">
const { rows } = defineProps<{
  rows: { header: string; value?: string }[]
}>()
</script>
