<template>
  <v-sheet
    rounded="lg"
    border
  >
    <div class="d-flex flex-column flex-md-row align-center">
      <v-table
        density="compact"
        class="my-2"
      >
        <tbody>
          <tr>
            <th>Book file</th>
            <td>{{ event.properties.name }}</td>
          </tr>
        </tbody>
        <tbody>
          <tr>
            <th>Page file name</th>
            <td>{{ event.properties['page file name'] }}</td>
          </tr>
        </tbody>
        <tbody>
          <tr>
            <th>Page number</th>
            <td>{{ event.properties['page number'] }}</td>
          </tr>
        </tbody>
        <tbody>
          <tr>
            <th>Page file size</th>
            <td>{{ event.properties['page file size'] }}</td>
          </tr>
        </tbody>
        <tbody>
          <tr>
            <th>Page media type</th>
            <td>{{ event.properties['page media type'] }}</td>
          </tr>
        </tbody>
      </v-table>

      <!--   TODO: add link to known page hash   -->
      <v-img
        width="200"
        height="200"
        contain
        :src="pageHashKnownThumbnailUrl(event.properties['page file hash'])"
        lazy-src="@/assets/cover.svg"
        class="my-2"
      >
        <template #placeholder>
          <div class="d-flex align-center justify-center fill-height">
            <v-progress-circular
              color="grey"
              indeterminate
            />
          </div>
        </template>
      </v-img>
    </div>
  </v-sheet>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { pageHashKnownThumbnailUrl } from '@/api/images'

const { event } = defineProps<{
  event: components['schemas']['HistoricalEventDto']
}>()
</script>
