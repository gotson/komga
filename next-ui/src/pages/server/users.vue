<template>
  <v-container
    fluid
    class="pa-0 pa-sm-4 h-100 h-sm-auto"
  >
    <EmptyStateNetworkError v-if="error" />

    <template v-else>
      <UserTable
        :users="users"
        :loading="isLoading"
        @add-user="showDialog('add')"
        @change-password="(user) => showDialog('password', user)"
        @edit-user="(user) => showDialog('edit', user)"
        @delete-user="(user) => showDialog('delete', user)"
        @enter-add-user="(target) => (dialogConfirmEdit.activator = target)"
        @enter-edit-user="(target) => (dialogConfirmEdit.activator = target)"
        @enter-change-password="(target) => (dialogConfirmEdit.activator = target)"
        @enter-delete-user="(target) => (dialogConfirm.activator = target)"
      />
    </template>
  </v-container>
</template>

<script lang="ts" setup>
import { useLibraries } from '@/colada/libraries'
import { commonMessages } from '@/utils/i18n/common-messages'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import UserDeletionWarning from '@/components/user/DeletionWarning.vue'
import UserFormCreateEdit from '@/components/user/form/CreateEdit.vue'
import UserFormChangePassword from '@/components/user/form/ChangePassword.vue'
import {
  useCreateUser,
  useDeleteUser,
  useUpdateUser,
  useUpdateUserPassword,
  useUsers,
} from '@/colada/users'
import type { UserCreationDto, UserDto } from '@/generated/openapi'
import { UserRole } from '@/types/UserRoles'

const intl = useIntl()

// API data
const { data: users, error, isLoading, refetch: refetchUsers } = useUsers()

onMounted(() => refetchUsers())

// Dialogs handling
// stores the user being actioned upon
const userRecord = ref<UserDto>()
// stores the ongoing action, so we can handle the action when the dialog is closed with changes
const currentAction = ref<DialogAction>()

const { confirmEdit: dialogConfirmEdit, confirm: dialogConfirm } = storeToRefs(useDialogsStore())

const { mutateAsync: mutateCreateUser } = useCreateUser()
const { mutateAsync: mutateUser } = useUpdateUser()
const { mutateAsync: mutateUserPassword } = useUpdateUserPassword()
const { mutateAsync: mutateDeleteUser } = useDeleteUser()
const { data: libraries } = useLibraries()

const messagesStore = useMessagesStore()
const display = useDisplay()

type DialogAction = 'add' | 'edit' | 'delete' | 'password'

function showDialog(action: DialogAction, user?: UserDto) {
  currentAction.value = action
  switch (action) {
    case 'add':
      dialogConfirmEdit.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Add user dialog title',
          defaultMessage: 'Add User',
          id: 'Bl30xt',
        }),
        scrollable: true,
        maxWidth: 600,
        closeOnSave: false,
        fullscreen: display.xs.value,
      }
      dialogConfirmEdit.value.slot = {
        component: markRaw(UserFormCreateEdit),
        props: {},
      }
      dialogConfirmEdit.value.record = {
        email: '',
        password: '',
        roles: [UserRole.PageStreaming, UserRole.FileDownload],
        sharedLibraries: {
          all: true,
          // we fill the array with all libraries for a nicer display in the edit dialog
          libraryIds: libraries.value?.map((x) => x.id) || [],
        },
        ageRestriction: {
          age: 0,
          restriction: 'NONE',
        },
      }
      dialogConfirmEdit.value.callback = handleDialogConfirmation
      break
    case 'edit':
      dialogConfirmEdit.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Edit user dialog title',
          defaultMessage: 'Edit User',
          id: 'Zh8AOV',
        }),
        subtitle: user?.email,
        scrollable: true,
        maxWidth: 600,
        closeOnSave: false,
        fullscreen: display.xs.value,
      }
      dialogConfirmEdit.value.slot = {
        component: markRaw(UserFormCreateEdit),
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
      }
      dialogConfirmEdit.value.callback = handleDialogConfirmation
      break
    case 'delete':
      dialogConfirm.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Delete user dialog title',
          defaultMessage: 'Delete User',
          id: '9XDmYO',
        }),
        subtitle: user?.email,
        maxWidth: 600,
        validateText: user?.email,
        mode: 'textinput',
        okText: intl.formatMessage({
          description: 'Delete user dialog: confirmation button text',
          defaultMessage: 'Delete',
          id: 'o8WeX3',
        }),
        closeOnSave: false,
        fullscreen: display.xs.value,
      }
      dialogConfirm.value.slotWarning = {
        component: markRaw(UserDeletionWarning),
        props: {},
      }
      dialogConfirm.value.callback = handleDialogConfirmation
      break
    case 'password':
      dialogConfirmEdit.value.dialogProps = {
        title: intl.formatMessage(commonMessages.changePasswordDialogTitle),
        subtitle: user?.email,
        maxWidth: 400,
        closeOnSave: false,
        fullscreen: display.xs.value,
      }
      dialogConfirmEdit.value.slot = {
        component: markRaw(UserFormChangePassword),
        props: {},
      }
      // password change initiated with an empty string
      dialogConfirmEdit.value.record = ''
      dialogConfirmEdit.value.callback = handleDialogConfirmation
  }
  userRecord.value = user
}

function handleDialogConfirmation(
  hideDialog: () => void,
  setLoading: (isLoading: boolean) => void,
) {
  let mutation: Promise<unknown> | undefined
  let successMessage: string | undefined

  setLoading(true)

  switch (currentAction.value) {
    case 'add':
      const newUser = dialogConfirmEdit.value.record as UserCreationDto
      mutation = mutateCreateUser(newUser)
      successMessage = intl.formatMessage(
        {
          description: 'Snackbar notification shown upon successful user creation',
          defaultMessage: 'User created: {email}',
          id: 'egrxd6',
        },
        {
          email: newUser.email,
        },
      )
      break
    case 'edit':
      const editUser = dialogConfirmEdit.value.record as UserDto
      mutation = mutateUser(editUser)
      successMessage = intl.formatMessage(
        {
          description: 'Snackbar notification shown upon successful user update',
          defaultMessage: 'User updated: {email}',
          id: 'kvbi4j',
        },
        {
          email: editUser.email,
        },
      )
      break
    case 'delete':
      mutation = mutateDeleteUser(userRecord.value!.id)
      successMessage = intl.formatMessage(
        {
          description: 'Snackbar notification shown upon successful user deletion',
          defaultMessage: 'User deleted: {email}',
          id: 'V/OYJE',
        },
        {
          email: userRecord.value!.email,
        },
      )
      break
    case 'password':
      mutation = mutateUserPassword({
        userId: userRecord.value!.id,
        newPassword: dialogConfirmEdit.value.record as string,
      })
      successMessage = intl.formatMessage(
        {
          description: "Snackbar notification shown upon successful user's password modification",
          defaultMessage: 'Password changed for user: {email}',
          id: 'JbF1nK',
        },
        {
          email: userRecord.value!.email,
        },
      )
      break
  }

  mutation
    ?.then(() => {
      hideDialog()
      if (successMessage) messagesStore.messages.push(successMessage)
    })
    .catch((error) => {
      messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
      setLoading(false)
    })
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
