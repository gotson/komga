import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { enableAutoUnmount } from '@vue/test-utils'
import { useGetLibrariesById } from '@/composables/libraries'
import { httpTyped } from '@/mocks/api/httpTyped'
import { mockLibraries } from '@/mocks/api/handlers/libraries'
import type { components } from '@/generated/openapi/komga'
import { CLIENT_SETTING_USER, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'
import { waitFor } from 'storybook/test'
import type { LibraryId } from '@/types/libraries'

beforeAll(() => server.listen())
beforeEach(() =>
  server.use(
    httpTyped.get('/api/v1/libraries', ({ response }) => {
      const bds = {
        ...mockLibraries[0],
        id: '3',
        name: 'BDs',
      } as components['schemas']['LibraryDto']
      const magazines = {
        ...mockLibraries[0],
        id: '4',
        name: 'Magazines',
      } as components['schemas']['LibraryDto']
      const manga = {
        ...mockLibraries[0],
        id: '5',
        name: 'Mangas',
      } as components['schemas']['LibraryDto']
      const libs = [bds, magazines, manga]
      return response(200).json(libs)
    }),
    httpTyped.get('/api/v1/client-settings/user/list', ({ response }) => {
      const userLibraries: Record<string, ClientSettingUserLibrary> = {
        '3': {
          unpinned: true,
        },
        '4': {
          unpinned: true,
        },
      }
      const settings: Record<string, components['schemas']['ClientSettingUserUpdateDto']> = {
        [CLIENT_SETTING_USER.NEXTUI_LIBRARIES]: {
          value: JSON.stringify(userLibraries),
        },
      }
      return response(200).json(settings)
    }),
  ),
)
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

enableAutoUnmount(afterEach)

describe('libraries composable', () => {
  test("when getting 'all' libraries then values are correct", async () => {
    await doTest('all', ['3', '4', '5'])
  })

  test("when getting 'pinned' libraries then values are correct", async () => {
    await doTest('pinned', ['5'])
  })

  test("when getting 'unpinned' libraries then values are correct", async () => {
    await doTest('unpinned', ['3', '4'])
  })

  test('when getting specific ID then values are correct', async () => {
    await doTest('4', ['4'])
  })

  test('when getting non-existent ID then values are correct', async () => {
    await doTest('ABC', [])
  })
})

async function doTest(libraryId: LibraryId, expectedIds: string[]) {
  createMockColada(() => useGetLibrariesById(libraryId))
  const { libraries } = useGetLibrariesById(libraryId)

  await waitFor(() => {
    if (libraries.value === undefined) throw new Error('data not fetched')
  })
  expect(libraries.value?.map((it) => it.id)).toStrictEqual(expectedIds)
}
