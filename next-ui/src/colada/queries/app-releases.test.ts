import { afterAll, afterEach, beforeAll, beforeEach, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { useAppReleases } from '@/colada/queries/app-releases'
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

test('when getting app releases then values are correct', async () => {
  const { latestRelease, isLatestVersion, refresh } = useAppReleases()

  await refresh()
  expect(latestRelease.value!.version).toBe('9.9.9')
  expect(isLatestVersion.value).toBe(true)
})
