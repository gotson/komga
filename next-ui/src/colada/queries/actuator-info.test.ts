import { afterAll, afterEach, beforeAll, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { useActuatorInfo } from '@/colada/queries/actuator-info'
import { createMockColada } from '@/mocks/pinia-colada'
import { http } from 'msw'
import { baseUrl, response401Unauthorized } from '@/mocks/api/handlers/base'
import { enableAutoUnmount } from '@vue/test-utils'
import type { ErrorCause } from '@/api/komga-client'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

enableAutoUnmount(afterEach)

test('when getting actuator-info then values are correct', async () => {
  createMockColada(useActuatorInfo)
  const { buildVersion, commitId, refresh } = useActuatorInfo()

  await refresh()
  expect(buildVersion.value).toBe('9.9.9')
  expect(commitId.value).toBe('ABC123')
})

test('when failing to get actuator-info then values are undefined', async () => {
  server.use(http.get(baseUrl + 'actuator/info', response401Unauthorized))

  createMockColada(useActuatorInfo)
  const { buildVersion, commitId, refresh, error } = useActuatorInfo()

  await refresh()
  expect(buildVersion.value).toBeUndefined()
  expect(commitId.value).toBeUndefined()
  expect((error.value?.cause as ErrorCause).status).toBe(401)
})
