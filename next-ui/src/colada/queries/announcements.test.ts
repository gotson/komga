import { afterAll, afterEach, beforeAll, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { useAnnouncements } from '@/colada/queries/announcements'
import { enableAutoUnmount } from '@vue/test-utils'

beforeAll(() => server.listen())
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

enableAutoUnmount(afterEach)

test('when getting announcements then values are correct', async () => {
  createMockColada(useAnnouncements)
  const { unreadCount, refresh } = useAnnouncements()

  await refresh()
  expect(unreadCount.value).toBe(1)
})
