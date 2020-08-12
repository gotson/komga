import { Shortcut } from '@/types/shortcuts'
import { ReadingDirection } from '@/types/enum-books'

export const shortcutsSettings = [
  new Shortcut('Left to Right',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.LEFT_TO_RIGHT)
    , 'l'),
  new Shortcut('Right to Left',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.RIGHT_TO_LEFT)
    , 'r'),
  new Shortcut('Vertical',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.VERTICAL)
    , 'v'),
  new Shortcut('Webtoon',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.WEBTOON)
    , 'w'),
  new Shortcut('Cycle scale',
    (ctx: any) => ctx.cycleScale()
    , 'c'),
]

export const shortcutsMenus = [
  new Shortcut('Show/hide toolbars',
    (ctx: any) => ctx.toggleToolbars()
    , 'm'),
  new Shortcut('Show/hide settings menu',
    (ctx: any) => ctx.toggleSettings()
    , 's'),
  new Shortcut('Show/hide thumbnails explorer',
    (ctx: any) => ctx.toggleExplorer()
    , 't'),
  new Shortcut('Show/hide help',
    (ctx: any) => ctx.toggleHelp()
    , 'h'),
  new Shortcut('Close',
    (ctx: any) => ctx.closeDialog()
    , 'Escape'),
]
