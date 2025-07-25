<template>
  <v-data-table
    :loading="loading"
    :items="users"
    :headers="headers"
    :hide-default-footer="hideFooter"
    fixed-header
    fixed-footer
    style="height: 100%"
    mobile-breakpoint="md"
  >
    <template #top>
      <v-toolbar flat>
        <v-toolbar-title>
          <v-icon
            color="medium-emphasis"
            icon="i-mdi:account-multiple"
            size="x-small"
            start
          />
          {{
            $formatMessage({
              description: 'Users table global header',
              defaultMessage: 'Users',
              id: 'c+hx0g',
            })
          }}
        </v-toolbar-title>

        <v-btn
          class="me-2"
          prepend-icon="i-mdi:plus"
          rounded="lg"
          :text="
            $formatMessage({
              description: 'Users table global header: add user button',
              defaultMessage: 'Add',
              id: 'X80giU',
            })
          "
          border
          :disabled="loading"
          @click="emit('addUser')"
          @mouseenter="emit('enterAddUser', $event.currentTarget)"
        />
      </v-toolbar>
    </template>

    <template #[`item.roles`]="{ value }">
      <div class="d-flex ga-1 flex-wrap">
        <v-chip
          v-for="role in value"
          :key="role"
          :color="getRoleColor(role)"
          :text="$formatMessage(userRolesMessages[role as UserRoles])"
          size="x-small"
          rounded
        />
      </div>
    </template>

    <template #[`item.activity`]="{ value }">
      <template v-if="value"
        >{{ $formatDate(value, { dateStyle: 'medium', timeStyle: 'short' }) }}
      </template>
      <template v-else
        >{{
          $formatMessage({
            description:
              'Shown in users table when there is no recent authentication activity for the user',
            defaultMessage: 'No recent activity',
            id: 'CvsH7/',
          })
        }}
      </template>
    </template>

    <template #[`item.actions`]="{ item: user }">
      <div class="d-flex ga-1 justify-end">
        <v-icon-btn
          v-tooltip:bottom="$formatMessage(messages.changePassword)"
          icon="i-mdi:lock-reset"
          :aria-label="$formatMessage(messages.changePassword)"
          @click="emit('changePassword', user)"
          @mouseenter="emit('enterChangePassword', $event.currentTarget)"
        />
        <v-icon-btn
          v-tooltip:bottom="$formatMessage(messages.editUser)"
          icon="i-mdi:pencil"
          :disabled="me?.id == user.id"
          :aria-label="$formatMessage(messages.editUser)"
          @click="emit('editUser', user)"
          @mouseenter="emit('enterEditUser', $event.currentTarget)"
        />
        <v-icon-btn
          v-tooltip:bottom="$formatMessage(messages.deleteUser)"
          icon="i-mdi:delete"
          :disabled="me?.id == user.id"
          :aria-label="$formatMessage(messages.deleteUser)"
          @click="emit('deleteUser', user)"
          @mouseenter="emit('enterDeleteUser', $event.currentTarget)"
        />
      </div>
    </template>
  </v-data-table>
</template>

<script lang="ts" setup>
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import { UserRoles, userRolesMessages } from '@/types/UserRoles'
import { defineMessage, useIntl } from 'vue-intl'

import { useCurrentUser } from '@/colada/users'

const intl = useIntl()

const { users = [], loading = false } = defineProps<{
  users?: components['schemas']['UserDto'][]
  loading?: boolean
}>()

const emit = defineEmits<{
  addUser: []
  enterAddUser: [target: Element]
  enterEditUser: [target: Element]
  enterDeleteUser: [target: Element]
  enterChangePassword: [target: Element]
  changePassword: [user: components['schemas']['UserDto']]
  editUser: [user: components['schemas']['UserDto']]
  deleteUser: [user: components['schemas']['UserDto']]
}>()

// API data
const { data: me } = useCurrentUser()

// Table
const hideFooter = computed(() => users.length < 11)
const headers = [
  {
    title: intl.formatMessage({
      description: 'User table header: user email',
      defaultMessage: 'Email',
      id: 'zfQq+w',
    }),
    key: 'email',
  },
  {
    title: intl.formatMessage({
      description: 'User table header: user latest activity',
      defaultMessage: 'Latest activity',
      id: 'y1P/K4',
    }),
    key: 'activity',
    value: (item: components['schemas']['UserDto']) => latestActivity[item.id],
  },
  {
    title: intl.formatMessage({
      description: 'User table header: user roles',
      defaultMessage: 'Roles',
      id: 'ut2gmo',
    }),
    value: 'roles',
    sortable: false,
  },
  {
    title: intl.formatMessage({
      description: 'User table header: user actions',
      defaultMessage: 'Actions',
      id: 'lvCpSX',
    }),
    key: 'actions',
    align: 'end',
    sortable: false,
  },
] as const // workaround for https://github.com/vuetifyjs/vuetify/issues/18901

function getRoleColor(role: UserRoles) {
  if (role === UserRoles.ADMIN) return 'error'
}

// store each user's latest activity in a map
// when the 'users' change, we call the API for each user
const latestActivity: Record<string, Date | undefined> = reactive({})

function getLatestActivity(userId: string) {
  komgaClient
    .GET('/api/v2/users/{id}/authentication-activity/latest', {
      params: {
        path: { id: userId },
      },
    })
    // unwrap the openapi-fetch structure on success
    .then((res) => (latestActivity[userId] = res.data?.dateTime))
    .catch(() => {})
}

watch(
  () => users,
  (users) => {
    if (users)
      for (const user of users) {
        getLatestActivity(user.id)
      }
  },
  { immediate: true },
)

const messages = {
  deleteUser: defineMessage({
    description: 'Tooltip for the delete user button in the users table',
    defaultMessage: 'Delete user',
    id: 'r6CqyT',
  }),
  editUser: defineMessage({
    description: 'Tooltip for the edit user button in the users table',
    defaultMessage: 'Edit user',
    id: 'K40g4r',
  }),
  changePassword: defineMessage({
    description: 'Tooltip for the change password button in the users table',
    defaultMessage: 'Change password',
    id: 'r7xCeA',
  }),
}
</script>
