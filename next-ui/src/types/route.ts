import type { RouteLocationRaw } from 'vue-router'

/**
 * Any valid Vue Router location in object form, avoiding string paths.
 */
export type RouteLocationObject = Exclude<RouteLocationRaw, string | { path: string }>
