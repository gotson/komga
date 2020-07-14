import { ReadingDirection } from '@/types/enum-books'

enum Shortcut {
  // Navigation
  SEEK_FORWARD = 'seekForward',
  SEEK_BACKWARD = 'seekBackward',
  // Vertical mode
  SEEK_UP = 'seekUp',
  SEEK_DOWN = 'seekDown',
  SEEK_BEGIN = 'seekBegin',
  SEEK_END = 'seekEnd',
  // SETTINGS
  DIR_LTR = 'directionLTR',
  DIR_RTL = 'directionRTL',
  DIR_VRT = 'directionVRT',
  TOGGLE_DOUBLE_PAGE = 'toggleDoublePage',
  CYCLE_SCALE = 'cycleScale',
  // OTHER
  TOGGLE_TOOLBAR = 'toggleToolbar',
  TOGGLE_MENU = 'toggleMenu',
  TOGGLE_THUMBNAIL_EXPLORER = 'toggleExplorer',
  ESCAPE = 'escape'
}

interface KeyMapping {
  [key: string]: Shortcut
}

type Action = (ctx: any) => void

interface Shortcuts {
  [key: string]: Action
}

// consider making this configurable on the server side?
const keyMapping = {
  'PageUp': Shortcut.SEEK_FORWARD,
  'ArrowRight': Shortcut.SEEK_FORWARD,
  'PageDown': Shortcut.SEEK_BACKWARD,
  'ArrowLeft': Shortcut.SEEK_BACKWARD,
  'ArrowDown': Shortcut.SEEK_DOWN,
  'ArrowUp': Shortcut.SEEK_UP,
  'Home': Shortcut.SEEK_BEGIN,
  'End': Shortcut.SEEK_END,
  'm': Shortcut.TOGGLE_TOOLBAR,
  's': Shortcut.TOGGLE_MENU,
  't': Shortcut.TOGGLE_THUMBNAIL_EXPLORER,
  'Escape': Shortcut.ESCAPE,
  'l': Shortcut.DIR_LTR,
  'r': Shortcut.DIR_RTL,
  'v': Shortcut.DIR_VRT,
  'd': Shortcut.TOGGLE_DOUBLE_PAGE,
  'f': Shortcut.CYCLE_SCALE,
} as KeyMapping

const shortcuts = {
  [Shortcut.SEEK_FORWARD]: (ctx: any) => {
    ctx.flipDirection ? ctx.prev() : ctx.next()
  },
  [Shortcut.SEEK_BACKWARD]: (ctx: any) => {
    ctx.flipDirection ? ctx.next() : ctx.prev()
  },
  [Shortcut.SEEK_UP]: (ctx: any) => { if (ctx.vertical) ctx.prev() },
  [Shortcut.SEEK_DOWN]: (ctx: any) => { if (ctx.vertical) ctx.next() },
  [Shortcut.SEEK_BEGIN]: (ctx: any) => { ctx.goToFirst() },
  [Shortcut.SEEK_END]: (ctx: any) => { ctx.goToLast() },
  [Shortcut.TOGGLE_TOOLBAR]: (ctx: any) => { ctx.toolbar = !ctx.toolbar },
  [Shortcut.TOGGLE_MENU]: (ctx: any) => { ctx.menu = !ctx.menu },
  [Shortcut.TOGGLE_THUMBNAIL_EXPLORER]: (ctx: any) => { ctx.showThumbnailsExplorer = !ctx.showThumbnailsExplorer },
  [Shortcut.TOGGLE_DOUBLE_PAGE]: (ctx: any) => ctx.toggleDoublePages(),
  [Shortcut.CYCLE_SCALE]: (ctx: any) => ctx.cycleScale(),
  [Shortcut.DIR_LTR]: (ctx: any) => ctx.changeReadingDir(ReadingDirection.LEFT_TO_RIGHT),
  [Shortcut.DIR_RTL]: (ctx: any) => ctx.changeReadingDir(ReadingDirection.RIGHT_TO_LEFT),
  [Shortcut.DIR_VRT]: (ctx: any) => ctx.changeReadingDir(ReadingDirection.VERTICAL),
  [Shortcut.ESCAPE]: (ctx: any) => {
    if (ctx.showThumbnailsExplorer) {
      ctx.showThumbnailsExplorer = false
      return
    }
    if (ctx.menu) {
      ctx.menu = false
      return
    }
    if (ctx.toolbar) {
      ctx.toolbar = false
      return
    }
    ctx.closeBook()
  },
} as Shortcuts

export function executeShortcut (ctx: any, e: KeyboardEvent): boolean {
  let k: string = e.key
  if (k in keyMapping) {
    let s: Shortcut = keyMapping[k]
    if (s in shortcuts) {
      let action: Action = shortcuts[s]
      if (action) {
        action(ctx)
        return true
      }
    }
  }
  return false
}
