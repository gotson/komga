// This can be directly added to any of your `.ts` files like `router.ts`
// It can also be added to a `.d.ts` file. Make sure it's included in
// project's tsconfig.json "files"
import 'vue-router'
import type { UserRoles } from '@/types/UserRoles'

// To ensure it is treated as a module, add at least one `export` statement
export {}

declare module 'vue-router' {
  interface RouteMeta {
    noAuth?: boolean
    requiresRole?: UserRoles
    /**
     * Indicates whether the scroll position should be persisted and restored for this page.
     */
    scrollable?: boolean
  }
}
