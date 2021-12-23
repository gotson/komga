import {Shortcut} from '@/types/shortcuts'
import {ReadingDirection} from '@/types/enum-books'

export const shortcutsSettings = [
  new Shortcut('bookreader.shortcuts.left_to_right',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.LEFT_TO_RIGHT)
    , 'l'),
  new Shortcut('bookreader.shortcuts.right_to_left',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.RIGHT_TO_LEFT)
    , 'r'),
  new Shortcut('bookreader.shortcuts.vertical',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.VERTICAL)
    , 'v'),
  new Shortcut('bookreader.shortcuts.webtoon',
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.WEBTOON)
    , 'w'),
  new Shortcut('bookreader.shortcuts.cycle_scale',
    (ctx: any) => ctx.cycleScale()
    , 'c'),
  new Shortcut('bookreader.shortcuts.fullscreen',
    (ctx: any) => ctx.switchFullscreen()
    , 'f'),
]

export const shortcutsMenus = [
  new Shortcut('bookreader.shortcuts.show_hide_toolbars',
    (ctx: any) => ctx.toggleToolbars()
    , 'm'),
  new Shortcut('bookreader.shortcuts.show_hide_settings',
    (ctx: any) => ctx.toggleSettings()
    , 's'),
  new Shortcut('bookreader.shortcuts.show_hide_thumbnails',
    (ctx: any) => ctx.toggleExplorer()
    , 't'),
  new Shortcut('bookreader.shortcuts.show_hide_help',
    (ctx: any) => ctx.toggleHelp()
    , 'h'),
  new Shortcut('bookreader.shortcuts.close',
    (ctx: any) => ctx.closeDialog()
    , 'Escape'),
]
