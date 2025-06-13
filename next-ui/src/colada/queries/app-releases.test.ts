import { afterAll, afterEach, beforeAll, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { useAppReleases } from '@/colada/queries/app-releases'
import { enableAutoUnmount } from '@vue/test-utils'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

enableAutoUnmount(afterEach)

test('when getting app releases then values are correct', async () => {
  createMockColada(useAppReleases)
  const { latestRelease, isLatestVersion, refresh } = useAppReleases()

  await refresh()
  expect(latestRelease.value!.version).toBe('9.9.9')
  expect(isLatestVersion.value).toBe(true)
})
