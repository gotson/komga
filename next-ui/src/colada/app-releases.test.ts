import { afterAll, afterEach, beforeAll, describe, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { useAppReleases } from '@/colada/app-releases'
import { enableAutoUnmount } from '@vue/test-utils'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

enableAutoUnmount(afterEach)

describe('colada releases', () => {
  test('when getting app releases then values are correct', async () => {
    createMockColada(useAppReleases)
    const { latestRelease, isLatestVersion, refresh, actuatorRefresh } = useAppReleases()

    await refresh()
    await actuatorRefresh()
    expect(latestRelease.value!.version).toBe('1.21.2')
    expect(isLatestVersion.value).toBe(true)
  })
})
