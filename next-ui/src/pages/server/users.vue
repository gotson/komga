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
            @mouseenter="dialogConfirmEdit.activator = $event.currentTarget"
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
            @mouseenter="dialogConfirmEdit.activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Edit user'"
            :icon="mdiPencil"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.EDIT, user)"
            @mouseenter="dialogConfirmEdit.activator = $event.currentTarget"
          />
          <v-icon-btn
            v-tooltip:bottom="'Delete user'"
            :icon="mdiDelete"
            :disabled="me?.id == user.id"
            @click="showDialog(ACTION.DELETE, user)"
            @mouseenter="dialogConfirm.activator = $event.currentTarget"
          />
        </div>
      </template>
    </v-data-table>
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
import NoticeUserDeletion from '@/components/notice/UserDeletion.vue'
import { useLibraries } from '@/colada/queries/libraries'
import { commonMessages } from '@/utils/i18n/common-messages'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'

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

const { confirmEdit: dialogConfirmEdit, confirm: dialogConfirm } = storeToRefs(useDialogsStore())

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
      dialogConfirmEdit.value.dialogProps = {
        title: 'Add User',
        maxWidth: 600,
      }
      dialogConfirmEdit.value.slot = {
        component: markRaw(FormUserEdit),
        props: {},
      }
      dialogConfirmEdit.value.record = {
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
      dialogConfirmEdit.value.recordUpdatedCallback = handleDialogConfirmation
      break
    case ACTION.EDIT:
      dialogConfirmEdit.value.dialogProps = {
        title: 'Edit User',
        subtitle: user?.email,
        maxWidth: 600,
      }
      dialogConfirmEdit.value.slot = {
        component: markRaw(FormUserEdit),
        props: {},
      }
      dialogConfirmEdit.value.record = {
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
      dialogConfirmEdit.value.recordUpdatedCallback = handleDialogConfirmation
      break
    case ACTION.DELETE:
      dialogConfirm.value.dialogProps = {
        title: 'Delete User',
        subtitle: user?.email,
        maxWidth: 600,
        validateText: user?.email,
        okText: 'Delete',
      }
      dialogConfirm.value.slotWarning = {
        component: markRaw(NoticeUserDeletion),
        props: {},
      }
      dialogConfirm.value.confirmCallback = handleDialogConfirmation
      break
    case ACTION.PASSWORD:
      dialogConfirmEdit.value.dialogProps = {
        title: 'Change Password',
        subtitle: user?.email,
        maxWidth: 400,
      }
      dialogConfirmEdit.value.slot = {
        component: markRaw(FormUserChangePassword),
        props: {},
      }
      // password change initiated with an empty string
      dialogConfirmEdit.value.record = ''
      dialogConfirmEdit.value.recordUpdatedCallback = handleDialogConfirmation
  }
  userRecord.value = user
}

function handleDialogConfirmation() {
  switch (currentAction.value) {
    case ACTION.ADD:
      mutateCreateUser(dialogConfirmEdit.value.record as components['schemas']['UserCreationDto'])
      break
    case ACTION.EDIT:
      mutateUser(dialogConfirmEdit.value.record as components['schemas']['UserDto'])
      break
    case ACTION.DELETE:
      mutateDeleteUser(userRecord.value!.id)
      break
    case ACTION.PASSWORD:
      mutateUserPassword({
        userId: userRecord.value!.id,
        newPassword: dialogConfirmEdit.value.record as string,
      })
      break
  }
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
