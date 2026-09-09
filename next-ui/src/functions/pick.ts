export function pick<T extends object, K extends keyof T>(obj: T, ...keys: K[]): Pick<T, K> {
  return Object.fromEntries(keys.map((key) => [key, obj[key]])) as Pick<T, K>
}

export function pickSchemaKeys<
  TSchema extends { entries: Record<string, unknown> },
  TObj extends object,
>(schema: TSchema, obj: TObj) {
  const keys = Object.keys(schema.entries) as (keyof TSchema['entries'] & keyof TObj)[]
  return pick(obj, ...keys)
}
