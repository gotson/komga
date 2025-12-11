<template>
  <v-container class="px-0">
    <v-row>
      <v-col cols="auto">
        <div class="text-subtitle-2 d-flex ga-2 align-center">
          <div>
            {{
              $formatMessage({
                description: 'Form add/edit library: Options - section header for analysis',
                defaultMessage: 'Analysis',
                id: 'O/3awV',
              })
            }}
          </div>
          <v-icon
            v-tooltip:bottom="$formatMessage(commonMessages.resourceIntensive)"
            size="small"
            color="warning"
            icon="i-mdi:alert-circle-outline"
          />
        </div>

        <v-checkbox
          v-model="model.hashFiles"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Options - hash files',
              defaultMessage: 'Compute hash for files',
              id: 'wJaip6',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description: 'Form add/edit library: Options - hash files - information tooltip',
                  defaultMessage: 'Required to restore from trash and detect duplicate files',
                  id: '/8sSxS',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>

        <v-checkbox
          v-model="model.hashPages"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Options - hash pages',
              defaultMessage: 'Compute hash for pages',
              id: '0qntYX',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description: 'Form add/edit library: Options - hash pages - information tooltip',
                  defaultMessage: 'Required for detecting duplicate pages',
                  id: 'Pj29A+',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>

        <v-checkbox
          v-model="model.hashKoreader"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Options - koreader hash',
              defaultMessage: 'Compute hash for KOReader',
              id: 'nXFVsQ',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description:
                    'Form add/edit library: Options - koreader hash - information tooltip',
                  defaultMessage: 'Enable this if you use KOReader Sync',
                  id: 'DNmepU',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>

        <v-checkbox
          v-model="model.analyzeDimensions"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Options - analyze page dimensions',
              defaultMessage: 'Analyze pages dimensions',
              id: 'STdfYg',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description:
                    'Form add/edit library: Options - analyze page dimensions - information tooltip',
                  defaultMessage: 'Required for the WebReader to detect landscape pages',
                  id: 'ByRsV9',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>
      </v-col>
    </v-row>

    <v-divider class="mb-4" />

    <v-row>
      <v-col>
        <div class="text-subtitle-2">
          {{
            $formatMessage({
              description: 'Form add/edit library: Options - section header for file management',
              defaultMessage: 'File management',
              id: 'rks1H9',
            })
          }}
        </div>
        <v-checkbox
          v-model="model.repairExtensions"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Options - repair extensions',
              defaultMessage: 'Automatically repair incorrect file extensions',
              id: 'RwuMl5',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.convertToCbz"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Options - convert to cbz',
              defaultMessage: 'Automatically convert CBR to CBZ',
              id: 'b1hvh9',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-divider class="mb-4" />

    <v-row>
      <v-col>
        <div class="text-subtitle-2">
          {{
            $formatMessage({
              description: 'Form add/edit library: Options - section header for series cover',
              defaultMessage: 'Series cover',
              id: 'Bewgy6',
            })
          }}
        </div>
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-select
          v-model="model.seriesCover"
          :items="seriesCoverOptions"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { SeriesCover, seriesCoverMessages } from '@/types/SeriesCover'
import { commonMessages } from '@/utils/i18n/common-messages'
import type { components } from '@/generated/openapi/komga'

type LibraryCreationOptions = Pick<
  components['schemas']['LibraryCreationDto'],
  | 'hashFiles'
  | 'hashPages'
  | 'hashKoreader'
  | 'analyzeDimensions'
  | 'repairExtensions'
  | 'convertToCbz'
  | 'seriesCover'
>

const model = defineModel<LibraryCreationOptions>({ required: true })

const intl = useIntl()

const seriesCoverOptions = Object.values(SeriesCover).map((x) => ({
  title: intl.formatMessage(seriesCoverMessages[x]),
  value: x,
}))
</script>
