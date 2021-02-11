import {Shortcut} from '@/types/shortcuts'
import i18n from "@/i18n";

export const shortcutsSettingsContinuous = [
  new Shortcut(i18n.t('bookreader.shortcuts.cycle_side_padding').toString(),
    (ctx: any) => ctx.cycleSidePadding()
    , 'p'),
]
