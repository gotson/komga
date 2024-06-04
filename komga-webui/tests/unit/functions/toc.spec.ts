import {TocEntry} from '@/types/epub'
import {flattenToc} from '@/functions/toc'

describe('Multiple levels', () => {
  const initial = [
    {title: '1'},
    {
      title: '2',
      children: [
        {title: '2.1'},
        {
          title: '2.2',
          children: [
            {
              title: '2.2.1',
              children: [
                {title: '2.2.1.1'},
              ],
            },
          ],
        },
      ],
    },
  ] as TocEntry[]

  test('given toc when flattened then it should be flat with correct levels', () => {
    const flattened = flattenToc(initial)

    expect(flattened.length).toEqual(6)

    expect(flattened[0].title).toEqual('1')
    expect(flattened[0].level).toEqual(0)
    expect(flattened[0].children).toEqual(undefined)

    expect(flattened[1].title).toEqual('2')
    expect(flattened[1].level).toEqual(0)
    expect(flattened[1].children).toEqual(undefined)

    expect(flattened[2].title).toEqual('2.1')
    expect(flattened[2].level).toEqual(1)
    expect(flattened[2].children).toEqual(undefined)

    expect(flattened[3].title).toEqual('2.2')
    expect(flattened[3].level).toEqual(1)
    expect(flattened[3].children).toEqual(undefined)

    expect(flattened[4].title).toEqual('2.2.1')
    expect(flattened[4].level).toEqual(2)
    expect(flattened[4].children).toEqual(undefined)

    expect(flattened[5].title).toEqual('2.2.1.1')
    expect(flattened[5].level).toEqual(3)
    expect(flattened[5].children).toEqual(undefined)
  })

  test('given toc when flattened to a max of 2 then it should be flat with correct levels', () => {
    const flattened = flattenToc(initial, 1)

    expect(flattened.length).toEqual(2)

    expect(flattened[0].title).toEqual('1')
    expect(flattened[0].level).toEqual(0)
    expect(flattened[0].children).toEqual(undefined)

    expect(flattened[1].title).toEqual('2')
    expect(flattened[1].level).toEqual(0)
    expect(flattened[1].children?.length).toEqual(4)

    expect(flattened[1].children!![0].title).toEqual('2.1')
    expect(flattened[1].children!![0].level).toEqual(1)
    expect(flattened[1].children!![1].title).toEqual('2.2')
    expect(flattened[1].children!![1].level).toEqual(1)
    expect(flattened[1].children!![2].title).toEqual('2.2.1')
    expect(flattened[1].children!![2].level).toEqual(2)
    expect(flattened[1].children!![3].title).toEqual('2.2.1.1')
    expect(flattened[1].children!![3].level).toEqual(3)
  })
})
