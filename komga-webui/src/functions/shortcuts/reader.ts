import {Shortcut} from '@/types/shortcuts'
import i18n from "@/i18n";

export const shortcutsAll = [
  new Shortcut(i18n.t('bookreader.shortcuts.first_page').toString(),
    (ctx: any) => {
      ctx.goToFirst()
    }, 'Home'),
  new Shortcut(i18n.t('bookreader.shortcuts.last_page').toString(),
    (ctx: any) => {
      ctx.goToLast()
    }, 'End'),
]
