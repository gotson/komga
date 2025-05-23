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

      <template #[`item.actions`]="{ item : user }">
        <div class="d-flex ga-1 justify-end">
          <v-icon-btn
            v-tooltip:bottom="'Reset password'"
            icon="mdi-lock-reset"
            @click="showDialog(ACTION.PASSWORD, user)"
            @mouseenter="activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Edit restrictions'"
            icon="mdi-book-lock"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.RESTRICTIONS, user)"
            @mouseenter="activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Edit user'"
            icon="mdi-pencil"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.EDIT, user)"
            @mouseenter="activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Delete user'"
            icon="mdi-delete"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.DELETE, user)"
            @mouseenter="activator = $event.currentTarget"
          />
        </div>
      </template>
    </v-data-table>

    <DialogConfirmEdit
      v-model:record="dialogRecord"
      :activator="activator"
      :title="dialogTitle"
      :subtitle="userRecord?.email"
      max-width="400"
      @update:record="handleDialogConfirmation()"
    >
      <template #text="{proxyModel}">
        <component
          :is="dialogComponent"
          v-model="proxyModel.value"
        />
      </template>
    </DialogConfirmEdit>
  </template>
</template>

<script lang="ts" setup>
import {useUsers} from '@/colada/queries/users.ts'
import {komgaClient} from '@/api/komga-client.ts'
import type {components} from '@/generated/openapi/komga'
import {useCurrentUser} from '@/colada/queries/current-user.ts'
import {UserRoles} from '@/types/UserRoles.ts'
import {useUpdateUser, useUpdateUserPassword} from '@/colada/mutations/update-user.ts'
import FormUserChangePassword from '@/components/forms/user/FormUserChangePassword.vue'
import FormUserRoles from '@/components/forms/user/FormUserRoles.vue'
import type {Component} from 'vue'

// API data
const {data: users, error, isLoading, refetch: refetchUsers} = useUsers()
const {data: me} = useCurrentUser()


// Table
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

onMounted(() => refetchUsers())


// Dialogs handling
// stores the user being actioned upon
const userRecord = ref<components["schemas"]["UserDto"]>()
// stores the ongoing action, so we can handle the action when the dialog is closed with changes
const currentAction = ref<ACTION>()
// the record passed to the dialog's form's model
const dialogRecord = ref<unknown>()
const activator = ref<Element>()
const dialogTitle = ref<string>()
// dynamic component for the dialog's inner form
const dialogComponent = shallowRef<Component>()

const {mutate: mutateUser} = useUpdateUser()
const {mutate: mutateUserPassword} = useUpdateUserPassword()

enum ACTION {
  EDIT, DELETE, RESTRICTIONS, PASSWORD
}

function showDialog(action: ACTION, user: components["schemas"]["UserDto"]) {
  currentAction.value = action
  switch (action) {
    case ACTION.EDIT:
      dialogTitle.value = 'Edit Roles'
      dialogComponent.value = FormUserRoles
      dialogRecord.value = user
      break;
    case ACTION.DELETE:
      dialogTitle.value = 'Delete User'
      dialogComponent.value = FormUserRoles
      dialogRecord.value = user
      break;
    case ACTION.RESTRICTIONS:
      dialogTitle.value = 'Edit Restrictions'
      dialogComponent.value = FormUserRoles
      dialogRecord.value = user
      break;
    case ACTION.PASSWORD:
      dialogTitle.value = 'Change Password'
      dialogComponent.value = FormUserChangePassword
      // password change initiated with an empty string
      dialogRecord.value = ''
  }
  userRecord.value = user
}

function handleDialogConfirmation() {
  switch (currentAction.value) {
    case ACTION.EDIT:
      mutateUser(dialogRecord.value as components["schemas"]["UserDto"])
      break;
    case ACTION.DELETE:
      break;
    case ACTION.RESTRICTIONS:
      break;
    case ACTION.PASSWORD:
      mutateUserPassword({
        userId: userRecord.value!.id,
        newPassword: dialogRecord.value as string,
      })
      break;
  }
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
