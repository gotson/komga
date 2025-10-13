<template>
  <v-data-table-server
    v-model:sort-by="sortBy"
    :loading="isLoading"
    :items="data?.content"
    :items-length="data?.totalElements || 0"
    :headers="headers"
    fixed-header
    fixed-footer
    multi-sort
    mobile-breakpoint="md"
    style="height: 100%"
    @update:options="updateOptions"
  >
    <template #top>
      <v-toolbar flat>
        <v-toolbar-title>
          <v-icon
            color="medium-emphasis"
            icon="i-mdi:account-key"
            size="x-small"
            start
          />
          {{
            $formatMessage({
              description: 'Authentication Activity table global header',
              defaultMessage: 'Authentication Activity',
              id: 'LaMAsc',
            })
          }}
        </v-toolbar-title>
      </v-toolbar>
    </template>

    <template #no-data>
      <EmptyStateNetworkError v-if="error" />
      <template v-else>
        {{
          $formatMessage({
            description: 'Authentication Activity table: shown when table has no data',
            defaultMessage: 'No recent activity',
            id: 'kGC6Gu',
          })
        }}
      </template>
    </template>

    <template #[`item.success`]="{ value }">
      <v-icon
        v-if="value"
        icon="i-mdi:check-circle-outline"
        color="success"
      />
      <v-icon
        v-else
        icon="i-mdi:error-outline"
        color="error"
      />
    </template>

    <template #[`item.dateTime`]="{ value }">
      {{ $formatDate(value, { dateStyle: 'medium', timeStyle: 'short' }) }}
    </template>
  </v-data-table-server>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { PageRequest, type SortItem } from '@/types/PageRequest'
import { useQuery } from '@pinia/colada'
import { authenticationActivityQuery, myAuthenticationActivityQuery } from '@/colada/users'

const intl = useIntl()

const { forMe = false } = defineProps<{ forMe?: boolean }>()

const sortBy = ref<SortItem[]>([{ key: 'dateTime', order: 'desc' }])

const headers = computed(() => {
  const h = []
  if (!forMe)
    h.push({
      title: intl.formatMessage({
        description: 'Authentication Activity table header: email',
        defaultMessage: 'Email',
        id: 'HHvDPs',
      }),
      key: 'email',
    })
  h.push(
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: IP address',
        defaultMessage: 'IP',
        id: 'YbkrN9',
      }),
      key: 'ip',
    },
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: User Agent',
        defaultMessage: 'User Agent',
        id: 'cWVIRm',
      }),
      key: 'userAgent',
    },
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: Successful authentication or not',
        defaultMessage: 'Success',
        id: 'XZ5lw4',
      }),
      key: 'success',
    },
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: Source',
        defaultMessage: 'Source',
        id: 'Zozcfh',
      }),
      key: 'source',
    },
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: API Key',
        defaultMessage: 'API Key',
        id: 'o1XnPU',
      }),
      key: 'apiKeyComment',
      sortable: false,
    },
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: Error',
        defaultMessage: 'Error',
        id: 'CVk1J6',
      }),
      key: 'error',
    },
    {
      title: intl.formatMessage({
        description: 'Authentication Activity table header: Date Time',
        defaultMessage: 'Date Time',
        id: 'CpNtjm',
      }),
      key: 'dateTime',
    },
  )
  return h
})

const pageRequest = ref<PageRequest>(new PageRequest())

const { data, isLoading, error } = useQuery(
  forMe ? myAuthenticationActivityQuery : authenticationActivityQuery,
  () => ({ ...pageRequest.value }),
)

function updateOptions({
  page,
  itemsPerPage,
  sortBy,
}: {
  page: number
  itemsPerPage: number
  sortBy: SortItem[]
}) {
  pageRequest.value = PageRequest.FromVuetify(page - 1, itemsPerPage, sortBy)
}
</script>
