import { afterAll, afterEach, beforeAll, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { useActuatorInfo } from '@/colada/queries/actuator-info'
import { mockPiniaColada } from '@/mocks/pinia-colada'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => {
  server.close()
  mockPiniaColada.unmount()
})

test('when getting actuator-info then values are correct', async () => {
  const { buildVersion, commitId, refresh } = useActuatorInfo()

  await refresh()
  expect(buildVersion.value).toBe('9.9.9')
  expect(commitId.value).toBe('ABC123')
})
