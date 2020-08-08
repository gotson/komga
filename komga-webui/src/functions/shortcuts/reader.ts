import { Shortcut } from '@/types/shortcuts'

export const shortcutsAll = [
  new Shortcut('First page',
    (ctx: any) => {
      ctx.goToFirst()
    }, 'Home'),
  new Shortcut('Last page',
    (ctx: any) => {
      ctx.goToLast()
    }, 'End'),
]
