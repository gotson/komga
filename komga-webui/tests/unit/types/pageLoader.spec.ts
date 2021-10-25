import {PageLoader} from '@/types/pageLoader'

describe('PageLoader', () => {
  const pageRequest = {} as PageRequest
  const loader = (pageable: PageRequest) => Promise.resolve({
    content: [1, 2, 3],
    last: true,
    number: 0,
  } as Page<number>)

  test('given page loader when loading next then it should return true', async () => {
    const pageLoader = new PageLoader(pageRequest, loader)

    expect(await pageLoader.loadNext()).toBeTruthy()
    expect(pageLoader.items).toStrictEqual([1, 2, 3])
  })

  test('given page loader on last page when loading next then it should return false', async () => {
    const pageLoader = new PageLoader(pageRequest, loader)

    expect(await pageLoader.loadNext()).toBeTruthy()
    expect(pageLoader.items).toStrictEqual([1, 2, 3])
    expect(await pageLoader.loadNext()).toBeFalsy()
  })

  test('given page loader when loading next then it should return true until last page is reached', async () => {
    const loader = (pageable: PageRequest) => Promise.resolve({
      content: pageable.page === 1 ? [4, 5, 6] : [1, 2, 3],
      last: pageable.page === 1,
      number: pageable.page || 0,
    } as Page<number>)
    const pageLoader = new PageLoader(pageRequest, loader)

    expect(await pageLoader.loadNext()).toBeTruthy()
    expect(pageLoader.items).toStrictEqual([1, 2, 3])
    expect(await pageLoader.loadNext()).toBeTruthy()
    expect(pageLoader.items).toStrictEqual([1, 2, 3, 4, 5, 6])
    expect(await pageLoader.loadNext()).toBeFalsy()
  })

  test('given exception when loading next then exception is rethrown', async () => {
    const loader = (pageable: PageRequest) => {
      throw new Error('boom')
    }
    const pageLoader = new PageLoader(pageRequest, loader)

    await expect(pageLoader.loadNext()).rejects.toEqual(new Error('boom'))
  })
})
