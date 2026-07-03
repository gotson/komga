import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, test } from 'vitest'
import { server } from '@/mocks/api/node'
import { createMockColada } from '@/mocks/pinia-colada'
import { enableAutoUnmount } from '@vue/test-utils'
import { useGetLibrariesById } from '@/composables/libraries'

import { mockLibraries } from '@/mocks/api/handlers/libraries'

import { ClientSettingUser, type ClientSettingUserLibrary } from '@/types/ClientSettingsUser'
import { waitFor } from 'storybook/test'
import type { LibraryId } from '@/types/libraries'
import type { ClientSettingUserUpdateDto, LibraryDto } from '@/generated/openapi'
import { handleGetLibraries, handleGetUserSettings } from '@/generated/openapi/msw.gen'

import { response200OK } from '@/mocks/api/utils'

beforeAll(() => server.listen())
beforeEach(() =>
  server.use(
    handleGetLibraries(() => {
      const bds = {
        ...mockLibraries[0],
        id: '3',
        name: 'BDs',
      } as LibraryDto
      const magazines = {
        ...mockLibraries[0],
        id: '4',
        name: 'Magazines',
      } as LibraryDto
      const manga = {
        ...mockLibraries[0],
        id: '5',
        name: 'Mangas',
      } as LibraryDto
      const libs = [bds, magazines, manga]
      return response200OK(libs)
    }),
    handleGetUserSettings(() => {
      const userLibraries: Record<string, ClientSettingUserLibrary> = {
        '3': {
          unpinned: true,
        },
        '4': {
          unpinned: true,
        },
      }
      const settings: Record<string, ClientSettingUserUpdateDto> = {
        [ClientSettingUser.NextUILibraries]: {
          value: JSON.stringify(userLibraries),
        },
      }
      return response200OK(settings)
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
