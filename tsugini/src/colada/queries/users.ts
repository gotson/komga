import { defineQuery, useQuery } from '@pinia/colada'
import { komgaClient } from 'api/komga-client'

export const useUsers = defineQuery(() => {
  return useQuery({
    key: () => ['users'],
    query: () =>
      komgaClient
        .GET('/api/v2/users')
        // unwrap the openapi-fetch structure on success
        .then((res) => res.data),
  })
})
