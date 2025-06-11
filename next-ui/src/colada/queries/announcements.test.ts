import { afterAll, afterEach, beforeAll, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { mockPiniaColada } from '@/mocks/pinia-colada'
import { useAnnouncements } from '@/colada/queries/announcements'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => {
  server.close()
  mockPiniaColada.unmount()
})

test('when getting announcements then values are correct', async () => {
  const { unreadCount, refresh } = useAnnouncements()

  await refresh()
  expect(unreadCount.value).toBe(1)
})
