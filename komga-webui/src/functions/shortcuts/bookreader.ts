import {Shortcut} from '@/types/shortcuts'
import {ReadingDirection} from '@/types/enum-books'
import i18n from "@/i18n";

export const shortcutsSettings = [
  new Shortcut(i18n.t('bookreader.shortcuts.left_to_right').toString(),
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.LEFT_TO_RIGHT)
    , 'l'),
  new Shortcut(i18n.t('bookreader.shortcuts.right_to_left').toString(),
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.RIGHT_TO_LEFT)
    , 'r'),
  new Shortcut(i18n.t('bookreader.shortcuts.vertical').toString(),
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.VERTICAL)
    , 'v'),
  new Shortcut(i18n.t('bookreader.shortcuts.webtoon').toString(),
    (ctx: any) => ctx.changeReadingDir(ReadingDirection.WEBTOON)
    , 'w'),
  new Shortcut(i18n.t('bookreader.shortcuts.cycle_scale').toString(),
    (ctx: any) => ctx.cycleScale()
    , 'c'),
]

export const shortcutsMenus = [
  new Shortcut(i18n.t('bookreader.shortcuts.show_hide_toolbars').toString(),
    (ctx: any) => ctx.toggleToolbars()
    , 'm'),
  new Shortcut(i18n.t('bookreader.shortcuts.show_hide_settings').toString(),
    (ctx: any) => ctx.toggleSettings()
    , 's'),
  new Shortcut(i18n.t('bookreader.shortcuts.show_hide_thumbnails').toString(),
    (ctx: any) => ctx.toggleExplorer()
    , 't'),
  new Shortcut(i18n.t('bookreader.shortcuts.show_hide_help').toString(),
    (ctx: any) => ctx.toggleHelp()
    , 'h'),
  new Shortcut(i18n.t('bookreader.shortcuts.close').toString(),
    (ctx: any) => ctx.closeDialog()
    , 'Escape'),
]
