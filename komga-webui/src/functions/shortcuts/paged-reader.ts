import { Shortcut } from '@/types/shortcuts'

export const shortcutsLTR = [
  new Shortcut('Previous page',
    (ctx: any) => {
      ctx.turnLeft()
    }, 'ArrowLeft', '←'),
  new Shortcut('Next page',
    (ctx: any) => {
      ctx.turnRight()
    }, 'ArrowRight', '→'),
]

export const shortcutsRTL = [
  new Shortcut('Previous page',
    (ctx: any) => {
      ctx.turnRight()
    }, 'ArrowRight', '→'),
  new Shortcut('Next page',
    (ctx: any) => {
      ctx.turnLeft()
    }, 'ArrowLeft', '←'),
]

export const shortcutsVertical = [
  new Shortcut('Previous page',
    (ctx: any) => {
      ctx.verticalPrev()
    }, 'ArrowUp', '↑'),
  new Shortcut('Next page',
    (ctx: any) => {
      ctx.verticalNext()
    }, 'ArrowDown', '↓'),
]

export const shortcutsSettingsPaged = [
  new Shortcut('Toggle double pages',
    (ctx: any) => ctx.toggleDoublePages()
    , 'd'),
]
