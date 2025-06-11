import { afterAll, afterEach, beforeAll, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { mockPiniaColada } from '@/mocks/pinia-colada'
import { useAppReleases } from '@/colada/queries/app-releases'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => {
  server.close()
  mockPiniaColada.unmount()
})

test('when getting app releases then values are correct', async () => {
  const { latestRelease, isLatestVersion, refresh } = useAppReleases()

  await refresh()
  expect(latestRelease.value!.version).toBe('9.9.9')
  expect(isLatestVersion.value).toBe(true)
})
