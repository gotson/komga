import { storeToRefs } from 'pinia'
import { useDialogsStore } from '@/stores/dialogs'
import { useIntl } from 'vue-intl'
import { useDisplay } from 'vuetify/framework'
import Details from '@/components/book/details/Details.vue'
import type { BookDetails } from '@/types/BookDetails'

export function useBookDetailsDialog() {
  const { simple: dialogSimple } = storeToRefs(useDialogsStore())
  const intl = useIntl()
  const display = useDisplay()

  const prepareDialog = (book: BookDetails, callback: () => void = () => {}) => {
    dialogSimple.value.dialogProps = {
      title: intl.formatMessage({
        description: 'Book details dialog title',
        defaultMessage: 'Book details',
        id: 'rtOXU6',
      }),
      cardTextProps: {
        class: 'px-0',
      },
      scrollable: true,
      fullscreen: display.xs.value,
    }
    dialogSimple.value.slot = {
      component: markRaw(Details),
      props: {
        book: book,
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
