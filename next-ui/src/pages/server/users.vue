<template>
  <v-alert
    v-if="error"
    type="error"
    variant="tonal"
  >
    Error loading data
  </v-alert>

  <template v-else>
    <v-data-table
      :loading="isLoading"
      :items="users"
      :headers="headers"
      :hide-default-footer="hideFooter"
    >
      <template #[`item.roles`]="{ value }">
        <div class="d-flex ga-1">
          <v-chip
            v-for="role in value"
            :key="role"
            :color="getRoleColor(role)"
            :text="role"
            size="x-small"
            rounded
          />
        </div>
      </template>

      <template #[`item.actions`]="{ item }">
        <div class="d-flex ga-1 justify-end">
          <v-icon-btn
            v-tooltip:bottom="'Reset password'"
            icon="mdi-lock-reset"
            @click="changePassword(item.id)"
          />
          <v-icon-btn
            v-tooltip:bottom="'Edit restrictions'"
            icon="mdi-book-lock"
            :disabled="me?.id == item.id"
            @click="editRestrictions(item.id)"
          />
          <v-icon-btn
            v-tooltip:bottom="'Edit user'"
            icon="mdi-pencil"
            :disabled="me?.id == item.id"
            @click="editUser(item.id)"
          />
          <v-icon-btn
            v-tooltip:bottom="'Delete user'"
            icon="mdi-delete"
            :disabled="me?.id == item.id"
            @click="deleteUser(item.id)"
          />
        </div>
      </template>
    </v-data-table>
  </template>
</template>

<script lang="ts" setup>
import {useUsers} from '@/colada/queries/users.ts'
import {komgaClient} from '@/api/komga-client.ts'
import type {components} from '@/generated/openapi/komga'
import {useCurrentUser} from '@/colada/queries/current-user.ts'
import {UserRoles} from '@/types/UserRoles.ts'

const {data: users, error, isLoading} = useUsers()
const {data: me} = useCurrentUser()

const hideFooter = computed(() => users.value && users.value.length < 11)

const headers = [
  {title: 'Email', key: 'email'},
  {title: 'Latest Activity', key: 'activity', value: (item: components["schemas"]["UserDto"]) => latestActivity[item.id]},
  {title: 'Roles', value: 'roles', sortable: false},
  {title: 'Actions', key: 'actions', align: 'end', sortable: false},
] as const // workaround for https://github.com/vuetifyjs/vuetify/issues/18901

function getRoleColor(role: UserRoles) {
  if(role === UserRoles.ADMIN) return 'error'
}


// store each user's latest activity in a map
// when the 'users' change, we call the API for each user
const latestActivity: Record<string, Date | undefined> = reactive({})

function getLatestActivity(userId: string) {
  komgaClient.GET('/api/v2/users/{id}/authentication-activity/latest', {
    params: {
      path: { id: userId }
    }
  })
    // unwrap the openapi-fetch structure on success
    .then((res) => latestActivity[userId] = res.data?.dateTime)
    .catch(() => {})
}

watch(users, (users) => {
  if(users) for (const user of users) {
    getLatestActivity(user.id)
  }
})


function editRestrictions(userId: string) {
  console.log('edit restrictions: ', userId)
}

function deleteUser(userId: string) {
  console.log('delete: ', userId)
}

function editUser(userId: string) {
  console.log('edit: ', userId)
}

function changePassword(userId: string) {
  console.log('change password: ', userId)
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
