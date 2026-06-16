<template>
  <v-container fluid>
    <v-row>
      <v-col
        cols="6"
        sm="3"
      >
        <ItemPoster :poster-url="readListPosterUrl(readList.id)" />
      </v-col>

      <v-col
        cols="6"
        sm="9"
      >
        <v-container class="pa-0">
          <v-row density="compact">
            <v-col>
              <div class="text-headline-small">{{ readList.name }}</div>
            </v-col>
          </v-row>

          <v-row density="compact">
            <v-col>
              <div class="text-body-medium">
                {{
                  readList.ordered
                    ? $formatMessage({
                        description: 'Read List view: manual ordering',
                        defaultMessage: 'Manual ordering',
                        id: '5Qha5m',
                      })
                    : $formatMessage({
                        description: 'Read List view: automatic ordering',
                        defaultMessage: 'Automatic ordering',
                        id: 'SuegoE',
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
        <v-col><ReadlistViewActions :read-list="readList" /></v-col>
      </v-row>

      <v-row v-if="readList.summary">
        <v-col>
          <ReadMore :text="readList.summary" />
        </v-col>
      </v-row>
    </v-container>
  </Teleport>
</template>

<script setup lang="ts">
import { readListPosterUrl } from '@/api/images'
import type { components } from '@/generated/openapi/komga'
import { useDisplay } from 'vuetify'

const display = useDisplay()
const id = useId()

defineProps<{
  readList: components['schemas']['ReadListDto']
}>()
</script>
