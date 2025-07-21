<template>
  <v-empty-state
    v-if="error"
    icon="i-mdi:connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

  <template v-else>
    <FragmentUserTable
      :users="users"
      :loading="isLoading"
      @add-user="showDialog(ACTION.ADD)"
      @change-password="(user) => showDialog(ACTION.PASSWORD, user)"
      @edit-user="(user) => showDialog(ACTION.EDIT, user)"
      @delete-user="(user) => showDialog(ACTION.DELETE, user)"
      @enter-add-user="(target) => (dialogConfirmEdit.activator = target)"
      @enter-edit-user="(target) => (dialogConfirmEdit.activator = target)"
      @enter-change-password="(target) => (dialogConfirmEdit.activator = target)"
      @enter-delete-user="(target) => (dialogConfirm.activator = target)"
    />
  </template>
</template>

<script lang="ts" setup>
import { type ErrorCause } from '@/api/komga-client'
import type { components } from '@/generated/openapi/komga'
import { UserRoles } from '@/types/UserRoles'
import { useLibraries } from '@/colada/libraries'
import { commonMessages } from '@/utils/i18n/common-messages'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify'
import UserDeletionWarning from '@/components/user/DeletionWarning.vue'
import UserFormCreateEdit from '@/fragments/fragment/user/form/CreateEdit.vue'
import UserFormChangePassword from '@/components/user/form/ChangePassword.vue'
import {
  useCreateUser,
  useDeleteUser,
  useUpdateUser,
  useUpdateUserPassword,
  useUsers,
} from '@/colada/users'

const intl = useIntl()

// API data
const { data: users, error, isLoading, refetch: refetchUsers } = useUsers()

onMounted(() => refetchUsers())

// Dialogs handling
// stores the user being actioned upon
const userRecord = ref<components['schemas']['UserDto']>()
// stores the ongoing action, so we can handle the action when the dialog is closed with changes
const currentAction = ref<ACTION>()

const { confirmEdit: dialogConfirmEdit, confirm: dialogConfirm } = storeToRefs(useDialogsStore())

const { mutateAsync: mutateCreateUser } = useCreateUser()
const { mutateAsync: mutateUser } = useUpdateUser()
const { mutateAsync: mutateUserPassword } = useUpdateUserPassword()
const { mutateAsync: mutateDeleteUser } = useDeleteUser()
const { data: libraries } = useLibraries()

const messagesStore = useMessagesStore()
const display = useDisplay()

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
        title: intl.formatMessage({
          description: 'Add user dialog title',
          defaultMessage: 'Add User',
          id: 'Bl30xt',
        }),
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
      dialogConfirmEdit.value.callback = handleDialogConfirmation
      break
    case ACTION.EDIT:
      dialogConfirmEdit.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Edit user dialog title',
          defaultMessage: 'Edit User',
          id: 'Zh8AOV',
        }),
        subtitle: user?.email,
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
      } as components['schemas']['UserUpdateDto']
      dialogConfirmEdit.value.callback = handleDialogConfirmation
      break
    case ACTION.DELETE:
      dialogConfirm.value.dialogProps = {
        title: intl.formatMessage({
          description: 'Delete user dialog title',
          defaultMessage: 'Delete User',
          id: '9XDmYO',
        }),
        subtitle: user?.email,
        maxWidth: 600,
        validateText: user?.email,
        okText: intl.formatMessage({
          description: 'Delete user dialog: confirmation button text',
          defaultMessage: 'Delete',
          id: 'o8WeX3',
        }),
        closeOnSave: false,
      }
      dialogConfirm.value.slotWarning = {
        component: markRaw(UserDeletionWarning),
        props: {},
      }
      dialogConfirm.value.callback = handleDialogConfirmation
      break
    case ACTION.PASSWORD:
      dialogConfirmEdit.value.dialogProps = {
        title: intl.formatMessage(commonMessages.changePasswordDialogTitle),
        subtitle: user?.email,
        maxWidth: 400,
        closeOnSave: false,
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
    case ACTION.ADD:
      const newUser = dialogConfirmEdit.value.record as components['schemas']['UserCreationDto']
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
    case ACTION.EDIT:
      const editUser = dialogConfirmEdit.value.record as components['schemas']['UserDto']
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
    case ACTION.DELETE:
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
    case ACTION.PASSWORD:
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
      if (successMessage) messagesStore.messages.push({ text: successMessage })
    })
    .catch((error) => {
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
      setLoading(false)
    })
}
</script>

<route lang="yaml">
meta:
  requiresRole: ADMIN
</route>
