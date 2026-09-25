import type { LocationQueryRaw } from 'vue-router'
import type { RouteLocationObject } from '@/types/route'

/**
 * Merges extra query parameters into an object-form route.
 * Preserves strict route name and params types.
 */
export function enrichRouteQuery<T extends RouteLocationObject>(
  location: T | undefined,
  extraQuery: LocationQueryRaw,
): T | undefined {
  if (!location) return undefined
  return {
    ...location,
    query: {
      ...(location.query || {}),
      ...extraQuery,
    },
  }
}
