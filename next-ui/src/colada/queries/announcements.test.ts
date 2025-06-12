import { afterAll, afterEach, beforeAll, beforeEach, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { useAnnouncements } from '@/colada/queries/announcements'
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

test('when getting announcements then values are correct', async () => {
  const { unreadCount, refresh } = useAnnouncements()

  await refresh()
  expect(unreadCount.value).toBe(1)
})
