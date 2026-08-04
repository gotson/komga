import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import { useMessagesStore } from '@/stores/messages'

import { commonMessages } from '@/utils/i18n/common-messages'
import type { LibraryCreationDto } from '@/generated/openapi'
import CreateEdit from '@/components/library/form/CreateEdit.vue'
import { getLibraryDefaults } from '@/functions/libraries'
import { useCreateLibrary } from '@/colada/libraries'

export function useCreateLibraryDialog() {
  const { confirmEdit: dialogConfirmEdit } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()
  const messagesStore = useMessagesStore()
  const { mutateAsync: mutateCreateLibrary } = useCreateLibrary()

  const prepareDialog = () => {
    dialogConfirmEdit.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Create library dialog title',
        defaultMessage: 'Create library',
        id: 'nuoJ1n',
      }),
      maxWidth: 600,
      okText: 'Create',
      cardTextClass: 'px-0',
      closeOnSave: false,
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogConfirmEdit.value.slot = {
      component: markRaw(CreateEdit),
      props: { createMode: true },
    }
    dialogConfirmEdit.value.record = getLibraryDefaults()
    dialogConfirmEdit.value.callback = (
      hideDialog: () => void,
      setLoading: (isLoading: boolean) => void,
    ) => {
      setLoading(true)

      const newLib = dialogConfirmEdit.value.record as LibraryCreationDto

      mutateCreateLibrary(newLib)
        .then(() => {
          hideDialog()
          messagesStore.messages.push({
            message: intl.formatMessage(
              {
                description: 'Snackbar notification shown upon successful library creation',
                defaultMessage: 'Library created: {library}',
                id: '+8++PW',
              },
              {
                library: newLib.name,
              },
            ),
          })
        })
        .catch((error) => {
          messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
          setLoading(false)
        })
    }
  }

  const activatorRef = computed({
    get: () => dialogConfirmEdit.value.activator,
    set: (val) => (dialogConfirmEdit.value.activator = val),
  })

  function showDialog() {
    dialogConfirmEdit.value.dialogProps.shown = true
  }

  return {
    prepareDialog: prepareDialog,
    activator: activatorRef,
    showDialog: showDialog,
  }
}
