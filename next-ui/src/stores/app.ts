// Utilities
import { defineStore } from 'pinia'
import { useDisplay } from 'vuetify'
import type { PresentationMode } from '@/types/libraries'
import type { PageSize, Paging } from '@/types/page'
import type { Sort } from '@/types/PageRequest'

export const useAppStore = defineStore(
  'app',
  () => {
    // persisted
    const drawer = ref(!useDisplay().mobile.value.valueOf())
    const theme = ref('system')
    const rememberMe = ref(false)
    const importBooksPath = ref('')
    const browsingPageSize = ref<PageSize>(20)
    const browsingPaging = ref<Paging>('scroll')
    const presentationMode = ref<Record<string, PresentationMode>>({})
    /**
     * Store the sort order per view.
     * Use the getter to ensure a default value is always set.
     */
    const sortActive = ref<Record<string, Sort[]>>({})
    const gridCardWidth = ref(150)
    // transient
    const reorderLibraries = ref(false)
    const sseUnavailable = ref(false)

    const isBrowsingPaged = computed(() => browsingPaging.value === 'paged')
    const isBrowsingScroll = computed(() => browsingPaging.value === 'scroll')

    const getPresentationMode = (key: string, defaultValue: PresentationMode) => {
      return computed({
        get: () => presentationMode.value[key] ?? (presentationMode.value[key] = defaultValue),
        set: (value) => {
          presentationMode.value[key] = value
        },
      })
    }

    const getSortActive = (key: string, defaultValue: Sort[]) => {
      return computed({
        get: () => sortActive.value[key] ?? (sortActive.value[key] = defaultValue),
        set: (value) => {
          sortActive.value[key] = value
        },
      })
    }

    return {
      drawer,
      theme,
      rememberMe,
      importBooksPath,
      browsingPageSize,
      browsingPaging,
      gridCardWidth,
      reorderLibraries,
      sseUnavailable,
      isBrowsingPaged,
      isBrowsingScroll,
      getPresentationMode,
      getSortActive,
    }
  },
  {
    persist: {
      key: 'komga.nextui.app',
      // explicitly state which keys are stored
      pick: [
        'drawer',
        'theme',
        'rememberMe',
        'importBooksPath',
        'browsingPageSize',
        'browsingPaging',
        'presentationMode',
        'sortActive',
        'gridCardWidth',
      ],
    },
  },
)
