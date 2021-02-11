import {Shortcut} from '@/types/shortcuts'
import i18n from "@/i18n";

export const shortcutsLTR = [
  new Shortcut(i18n.t('bookreader.shortcuts.previous_page').toString(),
    (ctx: any) => {
      ctx.turnLeft()
    }, 'ArrowLeft', '←'),
  new Shortcut(i18n.t('bookreader.shortcuts.next_page').toString(),
    (ctx: any) => {
      ctx.turnRight()
    }, 'ArrowRight', '→'),
]

export const shortcutsRTL = [
  new Shortcut(i18n.t('bookreader.shortcuts.previous_page').toString(),
    (ctx: any) => {
      ctx.turnRight()
    }, 'ArrowRight', '→'),
  new Shortcut(i18n.t('bookreader.shortcuts.next_page').toString(),
    (ctx: any) => {
      ctx.turnLeft()
    }, 'ArrowLeft', '←'),
]

export const shortcutsVertical = [
  new Shortcut(i18n.t('bookreader.shortcuts.previous_page').toString(),
    (ctx: any) => {
      ctx.verticalPrev()
    }, 'ArrowUp', '↑'),
  new Shortcut(i18n.t('bookreader.shortcuts.next_page').toString(),
    (ctx: any) => {
      ctx.verticalNext()
    }, 'ArrowDown', '↓'),
]

export const shortcutsSettingsPaged = [
  new Shortcut(i18n.t('bookreader.shortcuts.cycle_page_layout').toString(),
    (ctx: any) => ctx.cyclePageLayout()
    , 'd'),
]
