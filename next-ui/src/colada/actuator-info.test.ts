import { afterAll, afterEach, beforeAll, describe, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { useActuatorInfo } from '@/colada/actuator-info'
import { createMockColada } from '@/mocks/pinia-colada'
import { enableAutoUnmount } from '@vue/test-utils'
import type { ErrorCause } from '@/api/komga-client'
import { response401Unauthorized } from '@/mocks/api/handlers'
import { http } from 'msw'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

enableAutoUnmount(afterEach)

describe('colada actuator', () => {
  test('when getting actuator-info then values are correct', async () => {
    createMockColada(useActuatorInfo)
    const { buildVersion, commitId, refresh } = useActuatorInfo()

    await refresh()
    expect(buildVersion.value).toBe('1.21.2')
    expect(commitId.value).toBe('9be980d')
  })

  test('when failing to get actuator-info then values are undefined', async () => {
    server.use(http.get('*/actuator/info', response401Unauthorized))

    createMockColada(useActuatorInfo)
    const { buildVersion, commitId, refresh, error } = useActuatorInfo()

    await refresh()
    expect(buildVersion.value).toBeUndefined()
    expect(commitId.value).toBeUndefined()
    expect((error.value?.cause as ErrorCause).status).toBe(401)
  })
})
