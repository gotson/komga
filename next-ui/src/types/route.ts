import type { RouteLocationRaw } from 'vue-router'

export type Route = {
  title: string
  icon?: string
  to: RouteLocationRaw
}
