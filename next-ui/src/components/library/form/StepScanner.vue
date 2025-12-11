<template>
  <v-container class="px-0">
    <v-row>
      <v-col>
        <v-select
          v-model="model.scanInterval"
          :items="scanIntervalOptions"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Scanner - scan interval',
              defaultMessage: 'Scan interval',
              id: 'DwDa04',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-text-field
          v-model="model.oneshotsDirectory"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Scanner - one-shots directory',
              defaultMessage: 'One-shots directory',
              id: '6OXY1N',
            })
          "
          :hint="
            $formatMessage({
              description: 'Form add/edit library: Scanner - one-shots directory - hint',
              defaultMessage: 'Leave empty to disable',
              id: 't5ZhnZ',
            })
          "
          persistent-hint
          clearable
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-combobox
          v-model="model.scanDirectoryExclusions"
          multiple
          clearable
          chips
          closable-chips
          hide-details
          :label="
            $formatMessage({
              description: 'Form add/edit library: Scanner - directory exclusions',
              defaultMessage: 'Directory exclusions',
              id: 'bN2VbA',
            })
          "
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <div class="text-subtitle-1">
          {{
            $formatMessage({
              description: 'Form add/edit library: Scanner - file types selection header',
              defaultMessage: 'Scan for these file types',
              id: 'K+fQO2',
            })
          }}
        </div>
        <v-chip-group
          :model-value="scanTypes"
          multiple
          column
          @update:model-value="updateScanTypes"
        >
          <v-chip
            v-for="type in fileTypesOptions"
            :key="type.value"
            v-bind="type"
            filter
            rounded
            variant="outlined"
          />
        </v-chip-group>
      </v-col>
    </v-row>

    <v-row>
      <v-col cols="auto">
        <v-checkbox
          v-model="model.emptyTrashAfterScan"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Scanner - empty trash automatically',
              defaultMessage: 'Empty trash automatically after every scan',
              id: 'GySX8C',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.scanForceModifiedTime"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Scanner - Force directory modified time',
              defaultMessage: 'Force directory modified time',
              id: 'Xbf2fj',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description:
                    'Form add/edit library: Scanner - Force directory modified time - information tooltip',
                  defaultMessage: 'You should enable this if the library is hosted on Google Drive',
                  id: 'GyRV+/',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>

        <v-checkbox
          v-model="model.scanOnStartup"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Scanner - scan on startup',
              defaultMessage: 'Scan on application startup',
              id: 'TUxJCd',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ScanInterval, scanIntervalMessages } from '@/types/ScanInterval'
import { useIntl } from 'vue-intl'
import type { components } from '@/generated/openapi/komga'

type LibraryCreationScanner = Pick<
  components['schemas']['LibraryCreationDto'],
  | 'scanInterval'
  | 'oneshotsDirectory'
  | 'scanDirectoryExclusions'
  | 'scanCbx'
  | 'scanEpub'
  | 'scanPdf'
  | 'emptyTrashAfterScan'
  | 'scanForceModifiedTime'
  | 'scanOnStartup'
>

const model = defineModel<LibraryCreationScanner>({ required: true })

const intl = useIntl()

const scanIntervalOptions = Object.values(ScanInterval).map((x) => ({
  title: intl.formatMessage(scanIntervalMessages[x]),
  value: x,
}))

const scanTypes = computed(() => {
  const r = []
  if (model.value.scanCbx) r.push('cbx')
  if (model.value.scanPdf) r.push('pdf')
  if (model.value.scanEpub) r.push('epub')
  return r
})

function updateScanTypes(val: string[]) {
  model.value.scanCbx = val.includes('cbx')
  model.value.scanPdf = val.includes('pdf')
  model.value.scanEpub = val.includes('epub')
}

const fileTypesOptions = [
  {
    text: intl.formatMessage({
      description: 'Form add/edit library: Scanner - file types: comic book archives',
      defaultMessage: 'Comic Book archives',
      id: 'iu31A4',
    }),
    value: 'cbx',
  },
  {
    text: intl.formatMessage({
      description: 'Form add/edit library: Scanner - file types: pdf',
      defaultMessage: 'PDF',
      id: 'EOrAHc',
    }),
    value: 'pdf',
  },
  {
    text: intl.formatMessage({
      description: 'Form add/edit library: Scanner - file types: epub',
      defaultMessage: 'Epub',
      id: '5WMvLb',
    }),
    value: 'epub',
  },
]
</script>
