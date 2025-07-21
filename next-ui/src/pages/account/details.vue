<template>
  <v-empty-state
    v-if="error"
    icon="i-mdi:connection"
    :title="$formatMessage(commonMessages.somethingWentWrongTitle)"
    :text="$formatMessage(commonMessages.somethingWentWrongSubTitle)"
  />

  <template v-else-if="currentUser">
    <UserDetails :user="currentUser" />
    <v-btn
      :text="
        $formatMessage({
          description: 'User details screen: change password button',
          defaultMessage: 'Change password',
          id: 'sGsWvI',
        })
      "
      class="mt-8"
      @click="changePassword()"
      @mouseenter="dialogConfirmEdit.activator = $event.currentTarget"
    />
  </template>
</template>

<script lang="ts" setup>
import { useCurrentUser, useUpdateUserPassword } from '@/colada/users'
import { commonMessages } from '@/utils/i18n/common-messages'
import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import UserFormChangePassword from '@/components/user/form/ChangePassword.vue'
import type { ErrorCause } from '@/api/komga-client'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const { data: currentUser, error } = useCurrentUser()
const { mutateAsync: mutateUserPassword } = useUpdateUserPassword()

const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
const messagesStore = useMessagesStore()

function changePassword() {
  dialogConfirmEdit.value.dialogProps = {
    title: intl.formatMessage(commonMessages.changePasswordDialogTitle),
    subtitle: currentUser.value?.email,
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

function handleDialogConfirmation(
  hideDialog: () => void,
  setLoading: (isLoading: boolean) => void,
) {
  setLoading(true)

  mutateUserPassword({
    userId: currentUser.value!.id,
    newPassword: dialogConfirmEdit.value.record as string,
  })
    ?.then(() => {
      hideDialog()
      messagesStore.messages.push({
        text: intl.formatMessage({
          description:
            "Snackbar notification shown upon successful current user's password modification",
          defaultMessage: 'Password changed',
          id: '0FEy0X',
        }),
      })
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
