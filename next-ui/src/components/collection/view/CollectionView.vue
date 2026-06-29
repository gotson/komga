<template>
  <v-container fluid>
    <v-row>
      <v-col
        cols="6"
        sm="3"
      >
        <ItemPoster :poster-url="collectionPosterUrl(collection.id)" />
      </v-col>

      <v-col
        cols="6"
        sm="9"
      >
        <v-container class="pa-0">
          <v-row density="compact">
            <v-col>
              <div class="text-headline-small">{{ collection.name }}</div>
            </v-col>
          </v-row>

          <v-row density="compact">
            <v-col>
              <div class="text-body-medium">
                {{
                  collection.ordered
                    ? $formatMessage({
                        description: 'Collection view: manual ordering',
                        defaultMessage: 'Manual ordering',
                        id: '0eIJ3K',
                      })
                    : $formatMessage({
                        description: 'Collection view: automatic ordering',
                        defaultMessage: 'Automatic ordering',
                        id: 'wRjFWz',
                      })
                }}
              </div>
            </v-col>
          </v-row>
        </v-container>

        <div :id="`sm-${id}`" />
      </v-col>
    </v-row>

    <div :id="`xs-${id}`" />
  </v-container>

  <Teleport
    :to="`#${display.xs.value ? 'xs' : 'sm'}-${id}`"
    defer
  >
    <v-container class="px-0">
      <v-row>
        <v-col><CollectionViewActions :collection="collection" /></v-col>
      </v-row>
    </v-container>
  </Teleport>
</template>

<script setup lang="ts">
import { collectionPosterUrl } from '@/api/images'

import { useDisplay } from 'vuetify'
import type { CollectionDto } from '@/generated/openapi'

const display = useDisplay()
const id = useId()

defineProps<{
  collection: CollectionDto
}>()
</script>
