import { afterAll, afterEach, beforeAll, beforeEach, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { useActuatorInfo } from '@/colada/queries/actuator-info'
import { createMockColada } from '@/mocks/pinia-colada'
import { http } from 'msw'
import { baseUrl, response401Unauthorized } from '@/mocks/api/handlers/base'
import { VueWrapper } from '@vue/test-utils'

let mock: VueWrapper
beforeAll(() => server.listen())
beforeEach(() => {
  mock = createMockColada()
})
afterEach(() => {
  server.resetHandlers()
  mock.unmount()
})
afterAll(() => server.close())

test('when getting actuator-info then values are correct', async () => {
  const { buildVersion, commitId, refresh } = useActuatorInfo()

  await refresh()
  expect(buildVersion.value).toBe('9.9.9')
  expect(commitId.value).toBe('ABC123')
})

test('when failing to get actuator-info then values are undefined', async () => {
  server.use(http.get(baseUrl + 'actuator/info', response401Unauthorized))

  const { buildVersion, commitId, refresh, error } = useActuatorInfo()

  await refresh()
  expect(buildVersion.value).toBeUndefined()
  expect(commitId.value).toBeUndefined()
  expect(error.value).not.toBeUndefined()
})
