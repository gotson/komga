import { Shortcut } from '@/types/shortcuts'

export const shortcutsSettingsContinuous = [
  new Shortcut('Cycle side padding',
    (ctx: any) => ctx.cycleSidePadding()
    , 'p'),
]
