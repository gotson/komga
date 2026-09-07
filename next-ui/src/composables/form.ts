import { watchDeep } from '@vueuse/core'
import * as v from 'valibot'

export interface ValidatableField {
  isValid?: boolean | null
}

export function useFieldValidity(
  fields: Record<string, { value: ValidatableField | ValidatableField[] | null }>,
  onErrorCountChange: (errorCount: number) => void,
) {
  watchDeep(
    () => {
      // Computed inside the watcher so Vue tracks reactive updates and mounts
      const fieldRefs = Object.values(fields).flatMap((it) => {
        const val = it?.value
        if (!val) return []
        return Array.isArray(val) ? val : [val]
      })

      return fieldRefs.map((field) => field?.isValid)
    },
    (validity) => {
      onErrorCountChange(validity.filter((it) => it === false).length)
    },
  )
}

/**
 * Watches the model for any change to the keys passed in schemaOrKeys.
 * If a change is detected, and a lock key named `<key>Lock` exists, the lock key is updated to `true`.
 * @param model The object to watch
 * @param schemaOrKeys An array of keys, or a valibot schema from which keys will be extracted
 */
export function useLockWatcher<T extends Record<string, unknown>>(
  model: MaybeRefOrGetter<T>,
  schemaOrKeys:
    | v.ObjectSchema<v.ObjectEntries, v.ErrorMessage<v.ObjectIssue> | undefined>
    | (keyof T & string)[],
) {
  // Extract keys array whether passed a Valibot schema or a key array
  const keys = Array.isArray(schemaOrKeys)
    ? schemaOrKeys
    : 'entries' in schemaOrKeys
      ? Object.keys((schemaOrKeys as v.ObjectSchema<v.ObjectEntries, undefined>).entries)
      : []

  // Filter down to editable fields that actually have a matching '*Lock' property on the target
  const watchableKeys = keys.filter((key) => !key.endsWith('Lock'))

  // Generate a dedicated deep watcher for each property
  watchableKeys.forEach((key) => {
    watchDeep(
      () => toValue(model)?.[key],
      (newVal, oldVal) => {
        // Skip hydration: if the property starts as undefined and gets populated
        if (oldVal === undefined) return

        const target = toValue(model)
        if (!target) return

        const lockKey = `${key}Lock` as keyof T

        // If the callback fires, a mutation absolutely occurred. No diffing needed.
        if (lockKey in target && !target[lockKey]) {
          target[lockKey] = true as T[keyof T]
        }
      },
    )
  })
}
