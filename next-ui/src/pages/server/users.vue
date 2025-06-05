<template>
  <v-empty-state
    v-if="error"
    icon="mdi-connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

  <template v-else>
    <v-data-table
      :loading="isLoading"
      :items="users"
      :headers="headers"
      :hide-default-footer="hideFooter"
    >
      <template #top>
        <v-toolbar flat>
          <v-toolbar-title>
            <v-icon
              color="medium-emphasis"
              :icon="mdiAccountMultiple"
              size="x-small"
              start
            />
            Users
          </v-toolbar-title>

          <v-btn
            class="me-2"
            :prepend-icon="mdiPlus"
            rounded="lg"
            text="Add a User"
            border
            @click="showDialog(ACTION.ADD)"
            @mouseenter="activator = $event.currentTarget"
          />
        </v-toolbar>
      </template>

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

      <template #[`item.actions`]="{ item: user }">
        <div class="d-flex ga-1 justify-end">
          <v-icon-btn
            v-tooltip:bottom="'Change password'"
            :icon="mdiLockReset"
            @click="showDialog(ACTION.PASSWORD, user)"
            @mouseenter="activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Edit user'"
            :icon="mdiPencil"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.EDIT, user)"
            @mouseenter="activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Delete user'"
            :icon="mdiDelete"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.DELETE, user)"
            @mouseenter="activatorDelete = $event.currentTarget"
          />
        </div>
      </template>
    </v-data-table>

    <DialogConfirmEdit
      v-model:record="dialogRecord"
      :activator="activator"
      :title="dialogTitle"
      :subtitle="userRecord?.email"
      :max-width="currentAction === ACTION.PASSWORD ? 400 : 600"
      @update:record="handleDialogConfirmation()"
    >
      <template #text="{ proxyModel }">
        <component
          :is="dialogComponent"
          v-model="proxyModel.value"
        />
      </template>
    </DialogConfirmEdit>

    <DialogConfirm
      :activator="activatorDelete"
      :title="dialogTitle"
      :subtitle="userRecord?.email"
      ok-text="Delete"
      :validate-text="userRecord?.email"
      max-width="600"
      @confirm="handleDialogConfirmation()"
    >
      <template #warning>
        <v-alert
          type="warning"
          variant="tonal"
          class="mb-4"
        >
          <div>The user account will be deleted from this server.</div>
          <ul class="ps-8">
            <li>The read progress for this user account will be permanently deleted.</li>
            <li>Authentication activity for this user will be permanently deleted.</li>
          </ul>
          <div class="font-weight-bold mt-4">This action cannot be undone.</div>
        </v-alert>
      </template>
    </DialogConfirm>
  </template>
</template>

<script lang="ts" setup>
import mdiAccountMultiple from '~icons/mdi/account-multiple'
import mdiPlus from '~icons/mdi/plus'
import mdiLockReset from '~icons/mdi/lock-reset'
import mdiPencil from '~icons/mdi/pencil'
import mdiDelete from '~icons/mdi/delete'
import { useUsers } from '@/colada/queries/users'
import { komgaClient } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import { useCurrentUser } from '@/colada/queries/current-user'
import { UserRoles } from '@/types/UserRoles'
import {
  useCreateUser,
  useDeleteUser,
  useUpdateUser,
  useUpdateUserPassword,
} from '@/colada/mutations/update-user'
import FormUserChangePassword from '@/components/form/user/ChangePassword.vue'
import FormUserEdit from '@/components/form/user/Edit.vue'
import type { Component } from 'vue'
import { useLibraries } from '@/colada/queries/libraries'
import { commonMessages } from '@/utils/i18n/common-messages'

// API data
const { data: users, error, isLoading, refetch: refetchUsers } = useUsers()
const { data: me } = useCurrentUser()

// Table
const hideFooter = computed(() => users.value && users.value.length < 11)
const headers = [
  { title: 'Email', key: 'email' },
  {
    title: 'Latest Activity',
    key: 'activity',
    value: (item: components['schemas']['UserDto']) => latestActivity[item.id],
  },
  { title: 'Roles', value: 'roles', sortable: false },
  { title: 'Actions', key: 'actions', align: 'end', sortable: false },
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

watch(users, (users) => {
  if (users)
    for (const user of users) {
      getLatestActivity(user.id)
    }
})

onMounted(() => refetchUsers())

// Dialogs handling
// stores the user being actioned upon
const userRecord = ref<components['schemas']['UserDto']>()
// stores the ongoing action, so we can handle the action when the dialog is closed with changes
const currentAction = ref<ACTION>()
// the record passed to the dialog's form's model
const dialogRecord = ref<unknown>()
const activator = ref<Element>()
const activatorDelete = ref<Element>()
const dialogTitle = ref<string>()
// dynamic component for the dialog's inner form
const dialogComponent = shallowRef<Component>()

const { mutate: mutateCreateUser } = useCreateUser()
const { mutate: mutateUser } = useUpdateUser()
const { mutate: mutateUserPassword } = useUpdateUserPassword()
const { mutate: mutateDeleteUser } = useDeleteUser()
const { data: libraries } = useLibraries()

enum ACTION {
  ADD,
  EDIT,
  DELETE,
  PASSWORD,
}

function showDialog(action: ACTION, user?: components['schemas']['UserDto']) {
  currentAction.value = action
  switch (action) {
    case ACTION.ADD:
      dialogTitle.value = 'Add User'
      dialogComponent.value = FormUserEdit
      dialogRecord.value = {
        email: '',
        password: '',
        roles: [UserRoles.PAGE_STREAMING, UserRoles.FILE_DOWNLOAD],
        sharedLibraries: {
          all: true,
          // we fill the array with all libraries for a nicer display in the edit dialog
          libraryIds: libraries.value?.map((x) => x.id) || [],
        },
        ageRestriction: {
          age: 0,
          restriction: 'NONE',
        },
      } as components['schemas']['UserCreationDto']
      break
    case ACTION.EDIT:
      dialogTitle.value = 'Edit User'
      dialogComponent.value = FormUserEdit
      dialogRecord.value = {
        ...user,
        roles: user?.roles.filter((x) => x !== 'USER'),
        sharedLibraries: {
          all: user?.sharedAllLibraries,
          // we fill the array with all libraries for a nicer display in the edit dialog
          libraryIds: user?.sharedAllLibraries
            ? libraries.value?.map((x) => x.id) || []
            : user?.sharedLibrariesIds,
        },
        ageRestriction: user?.ageRestriction || {
          age: 0,
          restriction: 'NONE',
        },
      } as components['schemas']['UserUpdateDto']
      break
    case ACTION.DELETE:
      dialogTitle.value = 'Delete User'
      dialogComponent.value = FormUserEdit
      dialogRecord.value = user
      break
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
    case ACTION.ADD:
      mutateCreateUser(dialogRecord.value as components['schemas']['UserCreationDto'])
      break
    case ACTION.EDIT:
      mutateUser(dialogRecord.value as components['schemas']['UserDto'])
      break
    case ACTION.DELETE:
      mutateDeleteUser(userRecord.value!.id)
      break
    case ACTION.PASSWORD:
      mutateUserPassword({
        userId: userRecord.value!.id,
        newPassword: dialogRecord.value as string,
      })
      break
  }
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
