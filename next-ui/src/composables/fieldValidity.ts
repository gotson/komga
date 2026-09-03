export interface ValidatableField {
  isValid?: boolean | null
}

export function useFieldValidity(
  fields: Record<string, { value: ValidatableField | null }>,
  onErrorCountChange: (errorCount: number) => void,
) {
  const fieldRefs = Object.values(fields)

  watch(
    () => fieldRefs.map((field) => field.value?.isValid),
    (validity) => onErrorCountChange(validity.filter((it) => it === false).length),
  )
}
