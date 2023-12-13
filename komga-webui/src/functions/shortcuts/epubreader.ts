import {Shortcut} from '@/types/shortcuts'

export const shortcutsD2Reader = [
  new Shortcut('epubreader.shortcuts.previous',
    () => {
    }, 'CTRL+Space', 'CTRL + SPACE'),
  new Shortcut('epubreader.shortcuts.next',
    () => {
    }, 'Space', 'SPACE'),
]

export const shortcutsD2ReaderLTR = [
  new Shortcut('epubreader.shortcuts.previous',
    () => {
    }, 'ArrowLeft', '←'),
  new Shortcut('epubreader.shortcuts.next',
    () => {
    }, 'ArrowRight', '→'),
]

export const shortcutsD2ReaderRTL = [
  new Shortcut('epubreader.shortcuts.previous',
    () => {
    }, 'ArrowRight', '→'),
  new Shortcut('epubreader.shortcuts.next',
    () => {
    }, 'ArrowLeft', '←'),
]

export const epubShortcutsSettingsScroll = [
  new Shortcut('epubreader.shortcuts.scroll',
    (ctx: any) => ctx.changeLayout(true)
    , 'v'),
]

export const epubShortcutsSettings = [
  new Shortcut('epubreader.shortcuts.cycle_pagination',
    (ctx: any) => ctx.cyclePagination()
    , 'p'),
  new Shortcut('epubreader.shortcuts.cycle_viewing_theme',
    (ctx: any) => ctx.cycleViewingTheme()
    , 'a'),
  new Shortcut('epubreader.shortcuts.font_size_increase',
    (ctx: any) => ctx.changeFontSize(true)
    , '+'),
  new Shortcut('epubreader.shortcuts.font_size_decrease',
    (ctx: any) => ctx.changeFontSize(false)
    , '-'),
  new Shortcut('bookreader.shortcuts.fullscreen',
    (ctx: any) => ctx.switchFullscreen()
    , 'f'),
]

export const epubShortcutsMenus = [
  new Shortcut('bookreader.shortcuts.show_hide_toolbars',
    (ctx: any) => ctx.toggleToolbars()
    , 'm'),
  new Shortcut('bookreader.shortcuts.show_hide_settings',
    (ctx: any) => ctx.toggleSettings()
    , 's'),
  new Shortcut('epubreader.shortcuts.show_hide_toc',
    (ctx: any) => ctx.toggleTableOfContents()
    , 't'),
  new Shortcut('bookreader.shortcuts.show_hide_help',
    (ctx: any) => ctx.toggleHelp()
    , 'h'),
  new Shortcut('bookreader.shortcuts.close',
    (ctx: any) => ctx.closeDialog()
    , 'Escape'),
]
