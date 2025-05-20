import {useCurrentUser} from '@/colada/queries/current-user.ts'
import {UserRoles} from '@/types/UserRoles.ts'

export function useCurrentUserRole(role: UserRoles) {
  const {data} = useCurrentUser()

  const hasRole = computed(() => data.value?.roles.includes(role))

  return {hasRole}
}
