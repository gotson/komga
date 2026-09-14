import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import CompareBook from '@/components/book/compare/Compare.vue'
import type { BookDetails } from '@/types/BookDetails'

export function useCompareBookDialog() {
  const { simple: dialogSimple } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()

  const prepareDialog = (
    left: BookDetails,
    right: BookDetails,
    isUpgrade: boolean,
    callback: () => void = () => {},
  ) => {
    dialogSimple.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Compare books dialog title',
        defaultMessage: 'Compare books',
        id: '/LDx9p',
      }),
      cardTextProps: {
        class: 'px-0',
      },
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogSimple.value.slot = {
      component: markRaw(CompareBook),
      props: {
        left: left,
        right: right,
        isUpgrade: isUpgrade,
      },
    }

    dialogSimple.value.callback = () => callback()
  }

  const activatorRef = computed({
    get: () => dialogSimple.value.activator,
    set: (val) => (dialogSimple.value.activator = val),
  })

  function showDialog() {
    dialogSimple.value.dialogProps.shown = true
  }

  return {
    prepareDialog: prepareDialog,
    activator: activatorRef,
    showDialog: showDialog,
  }
}
