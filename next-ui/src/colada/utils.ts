/**
 * Convenience function to create a refresh/refetch function for queries
 * that depend on other queries.
 * @param main
 * @param secondary
 */
export function combinePromises(
  main: (throwOnError: boolean) => Promise<unknown>,
  secondary: (() => Promise<unknown>)[],
): (throwOnError?: boolean) => Promise<unknown> {
  return async function (throwOnError: boolean = false) {
    await Promise.allSettled(secondary.map((it) => it()))
    return main(throwOnError)
  }
}
