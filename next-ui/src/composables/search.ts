import type { components } from '@/generated/openapi/komga'

export function useSearchConditionLibraries(
  libraries: MaybeRefOrGetter<components['schemas']['LibraryDto'][] | undefined>,
) {
  const condition = computed(() => {
    return {
      anyOf:
        toValue(libraries)?.map((it) => ({
          libraryId: {
            operator: 'Is',
            value: it.id,
          },
        })) ?? [],
    }
  })

  return {
    librariesCondition: condition,
  }
}
