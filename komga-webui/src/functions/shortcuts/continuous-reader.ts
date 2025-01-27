import {Shortcut} from '@/types/shortcuts'

export const shortcutsSettingsContinuous = [
  new Shortcut('bookreader.shortcuts.cycle_side_padding',
    (ctx: any) => ctx.cycleSidePadding()
    , 'p'),
  new Shortcut('bookreader.shortcuts.cycle_page_margin',
    (ctx: any) => ctx.cyclePageMargin()
    , 'n'),
]
