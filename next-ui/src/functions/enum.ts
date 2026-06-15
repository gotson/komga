export function getEnumValueFromString<T extends Record<string, string | number>>(
  enumObj: T,
  value: string,
): T[keyof T] | undefined {
  if (Object.values(enumObj).includes(value)) {
    return value as T[keyof T]
  }
  return undefined
}
