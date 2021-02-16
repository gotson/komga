import {Shortcut} from '@/types/shortcuts'

export const shortcutsAll = [
  new Shortcut('bookreader.shortcuts.first_page',
    (ctx: any) => {
      ctx.goToFirst()
    }, 'Home'),
  new Shortcut('bookreader.shortcuts.last_page',
    (ctx: any) => {
      ctx.goToLast()
    }, 'End'),
]
