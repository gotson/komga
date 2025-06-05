<template>
  <KEmptyState
    v-if="error"
    icon="mdi-connection"
    icon-size="250px"
    avatar-color="grey-4"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

  <template v-else>
    <QTable
      :columns="columns"
      :rows="users ? users : []"
      :loading="isLoading"
      :rows-per-page-options="[10]"
      :hide-bottom="hideFooter"
    >
      <template #top>
        <q-toolbar>
          <q-toolbar-title>
            <q-icon
              name="mdi-account-multiple"
              left
            />
            Users
          </q-toolbar-title>

          <q-btn
            icon="mdi-plus"
            label="Add a user"
            @click="addUser()"
          />
        </q-toolbar>
      </template>

      <template #[`body-cell-roles`]="props">
        <q-td :props="props">
          <q-chip
            v-for="role in props.value"
            :key="role"
            :class="getRoleClass(role)"
            :label="role"
            size="sm"
          />
        </q-td>
      </template>

      <template #[`body-cell-actions`]="props">
        <q-td :props="props">
          <q-btn
            icon="mdi-lock-reset"
            round
            flat
            @click="changePassword(props.row)"
          >
            <q-tooltip class="text-body2">Change password</q-tooltip>
          </q-btn>
          <q-btn
            icon="mdi-pencil"
            round
            flat
            :disable="me?.id == props.row.id"
            :color="me?.id == props.row.id ? 'grey' : undefined"
            @click="editUser(props.row)"
          >
            <q-tooltip
              v-if="me?.id !== props.row.id"
              class="text-body2"
              >Edit user</q-tooltip
            >
          </q-btn>
          <q-btn
            icon="mdi-delete"
            round
            flat
            :disable="me?.id == props.row.id"
            :color="me?.id == props.row.id ? 'grey' : undefined"
            @click="showDialog(ACTION.DELETE, props.row)"
          >
            <q-tooltip
              v-if="me?.id !== props.row.id"
              class="text-body2"
              >Delete user</q-tooltip
            >
          </q-btn>
        </q-td>
      </template>
    </QTable>

    <!--    <DialogConfirmEdit-->
    <!--      v-model:record="dialogRecord"-->
    <!--      :activator="activator"-->
    <!--      :title="dialogTitle"-->
    <!--      :subtitle="userRecord?.email"-->
    <!--      :max-width="currentAction === ACTION.PASSWORD ? 400 : 600"-->
    <!--      @update:record="handleDialogConfirmation()"-->
    <!--    >-->
    <!--      <template #text="{ proxyModel }">-->
    <!--        <component-->
    <!--          :is="dialogComponent"-->
    <!--          v-model="proxyModel.value"-->
    <!--        />-->
    <!--      </template>-->
    <!--    </DialogConfirmEdit>-->

    <!--    <DialogConfirm-->
    <!--      :activator="activatorDelete"-->
    <!--      :title="dialogTitle"-->
    <!--      :subtitle="userRecord?.email"-->
    <!--      ok-text="Delete"-->
    <!--      :validate-text="userRecord?.email"-->
    <!--      max-width="600"-->
    <!--      @confirm="handleDialogConfirmation()"-->
    <!--    >-->
    <!--      <template #warning>-->
    <!--        <v-alert-->
    <!--          type="warning"-->
    <!--          variant="tonal"-->
    <!--          class="mb-4"-->
    <!--        >-->
    <!--          <div>The user account will be deleted from this server.</div>-->
    <!--          <ul class="ps-8">-->
    <!--            <li>The read progress for this user account will be permanently deleted.</li>-->
    <!--            <li>Authentication activity for this user will be permanently deleted.</li>-->
    <!--          </ul>-->
    <!--          <div class="font-weight-bold mt-4">This action cannot be undone.</div>-->
    <!--        </v-alert>-->
    <!--      </template>-->
    <!--    </DialogConfirm>-->
  </template>
</template>

<script lang="ts" setup>
import { useUsers } from 'colada/queries/users'
import { komgaClient } from 'api/komga-client'
import type { components } from 'openapi/komga'
import { useCurrentUser } from 'colada/queries/current-user'
import { UserRoles } from 'types/UserRoles'
import {
  useCreateUser,
  useDeleteUser,
  useUpdateUser,
  useUpdateUserPassword,
} from 'colada/mutations/update-user'
import { useLibraries } from 'colada/queries/libraries'
import { commonMessages } from 'utils/i18n/common-messages'
import { useQuasar } from 'quasar'
import FormUserChangePassword from 'components/form/user/ChangePassword.vue'
import FormUserEdit from 'components/form/user/Edit.vue'

// API data
const { data: users, error, isLoading, refetch: refetchUsers } = useUsers()
const { data: me } = useCurrentUser()

// Table
const hideFooter = computed(() => users.value && users.value.length < 11)
const columns = [
  {
    name: 'email',
    label: 'Email',
    field: 'email',
    align: 'left',
    sortable: true,
  },
  {
    name: 'activity',
    label: 'Latest Activity',
    field: (item: components['schemas']['UserDto']) => latestActivity[item.id],
    align: 'left',
    sortable: true,
  },
  {
    name: 'roles',
    label: 'Roles',
    field: 'roles',
    align: 'left',
    sortable: false,
  },
  {
    name: 'actions',
    label: 'Actions',
    field: () => '',
    align: 'right',
    sortable: false,
  },
]

function getRoleClass(role: UserRoles) {
  if (role === UserRoles.ADMIN) return 'chip-negative'
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

const $q = useQuasar()

function changePassword(user: components['schemas']['UserDto']) {
  $q.dialog({
    component: FormUserChangePassword,
    componentProps: {
      title: 'Change Password',
      subtitle: user.email,
    },
  }).onOk((newPassword: string) => {
    console.log('new password:', newPassword)
  })
}

function addUser() {
  $q.dialog({
    component: FormUserEdit,
    componentProps: {
      title: 'Add User',
    },
  }).onOk((user: string) => {
    console.log('add user:', user)
  })
}

function editUser(user: components['schemas']['UserDto']) {
  $q.dialog({
    component: FormUserEdit,
    componentProps: {
      title: 'Edit User',
      subtitle: user.email,
      user: {
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
      } as components['schemas']['UserUpdateDto'],
    },
  }).onOk((editedUser: string) => {
    console.log('edited user:', editedUser)
  })
}

function showDialog(action: ACTION, user?: components['schemas']['UserDto']) {
  currentAction.value = action
  // switch (action) {
  //   case ACTION.ADD:
  //     dialogTitle.value = 'Add User'
  //     dialogComponent.value = FormUserEdit
  //     dialogRecord.value = {
  //       email: '',
  //       password: '',
  //       roles: [UserRoles.PAGE_STREAMING, UserRoles.FILE_DOWNLOAD],
  //       sharedLibraries: {
  //         all: true,
  //         // we fill the array with all libraries for a nicer display in the edit dialog
  //         libraryIds: libraries.value?.map((x) => x.id) || [],
  //       },
  //       ageRestriction: {
  //         age: 0,
  //         restriction: 'NONE',
  //       },
  //     } as components['schemas']['UserCreationDto']
  //     break
  //   case ACTION.EDIT:
  //     dialogTitle.value = 'Edit User'
  //     dialogComponent.value = FormUserEdit
  //     dialogRecord.value = {
  //       ...user,
  //       roles: user?.roles.filter((x) => x !== 'USER'),
  //       sharedLibraries: {
  //         all: user?.sharedAllLibraries,
  //         // we fill the array with all libraries for a nicer display in the edit dialog
  //         libraryIds: user?.sharedAllLibraries
  //           ? libraries.value?.map((x) => x.id) || []
  //           : user?.sharedLibrariesIds,
  //       },
  //       ageRestriction: user?.ageRestriction || {
  //         age: 0,
  //         restriction: 'NONE',
  //       },
  //     } as components['schemas']['UserUpdateDto']
  //     break
  //   case ACTION.DELETE:
  //     dialogTitle.value = 'Delete User'
  //     dialogComponent.value = FormUserEdit
  //     dialogRecord.value = user
  //     break
  //   case ACTION.PASSWORD:
  //     dialogTitle.value = 'Change Password'
  //     dialogComponent.value = FormUserChangePassword
  //     // password change initiated with an empty string
  //     dialogRecord.value = ''
  // }
  userRecord.value = user
}
//
// function handleDialogConfirmation() {
//   switch (currentAction.value) {
//     case ACTION.ADD:
//       mutateCreateUser(dialogRecord.value as components['schemas']['UserCreationDto'])
//       break
//     case ACTION.EDIT:
//       mutateUser(dialogRecord.value as components['schemas']['UserDto'])
//       break
//     case ACTION.DELETE:
//       mutateDeleteUser(userRecord.value!.id)
//       break
//     case ACTION.PASSWORD:
//       mutateUserPassword({
//         userId: userRecord.value!.id,
//         newPassword: dialogRecord.value as string,
//       })
//       break
//   }
// }
</script>
<style scoped lang="scss"></style>
<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
