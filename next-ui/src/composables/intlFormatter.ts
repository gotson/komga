import { useIntl } from 'vue-intl'
import type { SortOption, SortOptionDescriptor } from '@/types/sort'

export function useIntlFormatter() {
  const intl = useIntl()

  function convertSortOptionDescriptor(sortDescriptor: SortOptionDescriptor): SortOption {
    return {
      label: intl.formatMessage(sortDescriptor.message),
      key: sortDescriptor.key,
      initialOrder: sortDescriptor.initialOrder,
      invertible: sortDescriptor.invertible,
    }
  }

  return { convertSortOptionDescriptor }
}
