import { ReadingDirection } from '@/types/enum-books'

interface Map<V> {
  [key: string]: V
}
class MultiMap<V> {
  dict: Map<V[]> = {}

  add (key:string, value: V) {
    this.dict[key] = (this.dict[key]?.concat([value])) || [value]
  }

  get (key: string): V[] {
    return this.dict[key]
  }

  items () {
    return Object.keys(this.dict).map((k) => ({ key: k, value: this.dict[k] }))
  }
}

type Action = (ctx: any) => void

class Shortcut {
  name: string
  category: string
  description: string
  action: Action

  keys: string[]

  constructor (name: string, category: string, description: string, action: Action, keys: string[]) {
    this.name = name
    this.category = category
    this.description = description
    this.action = action
    this.keys = keys
  }

  execute (ctx: any): boolean {
    console.log(this.name)
    this.action(ctx)
    return true
  }
}

const KEY_DISPLAY = {
  'ArrowRight': 'mdi-arrow-right',
  'ArrowLeft': 'mdi-arrow-left',
  'PageUp': 'PgUp',
  'PageDown': 'PgDn',
  'ArrowUp': 'mdi-arrow-up',
  'ArrowDown': 'mdi-arrow-down',
  'Escape': 'Esc',
} as Map<string>

const SHORTCUTS: Shortcut[] = []

function shortcut (name: string, category: string, description: string, action: Action, ...keys: string[]) {
  SHORTCUTS.push(new Shortcut(name, category, description, action, keys))
}

enum ShortcutCategory {
  READER_NAVIGATION = 'Reader Navigation',
  READER_SETTINGS = 'Reader Settings',
  MENUS = 'Menus'
}

// Reader Navigation
shortcut('seekForward', ShortcutCategory.READER_NAVIGATION, 'Next Page',
  (ctx: any) => {
    ctx.flipDirection ? ctx.prev() : ctx.next()
  }, 'PageUp', 'ArrowRight')

shortcut('seekBackward', ShortcutCategory.READER_NAVIGATION, 'Prev Page',
  (ctx: any) => {
    ctx.flipDirection ? ctx.next() : ctx.prev()
  }, 'PageDown', 'ArrowLeft')

shortcut('seekUp', ShortcutCategory.READER_NAVIGATION, 'Prev Page (Vertical)',
  (ctx: any) => {
    if (ctx.vertical) {
      ctx.prev()
    }
  }
  , 'ArrowUp')

shortcut('seekDown', ShortcutCategory.READER_NAVIGATION, 'Next Page (Vertical)',
  (ctx: any) => {
    if (ctx.vertical) {
      ctx.next()
    }
  }
  , 'ArrowDown')

shortcut('seekBegin', ShortcutCategory.READER_NAVIGATION, 'Goto First Page',
  (ctx: any) => {
    ctx.goToFirst()
  }
  , 'Home')

shortcut('seekEnd', ShortcutCategory.READER_NAVIGATION, 'Goto Last Page',
  (ctx: any) => {
    ctx.goToLast()
  }
  , 'End')

// Reader Settings

shortcut('directionLTR', ShortcutCategory.READER_SETTINGS, 'Direction: Left to Right',
  (ctx: any) => ctx.changeReadingDir(ReadingDirection.LEFT_TO_RIGHT)
  , 'l')

shortcut('directionRTL', ShortcutCategory.READER_SETTINGS, 'Direction: Right to Left',
  (ctx: any) => ctx.changeReadingDir(ReadingDirection.RIGHT_TO_LEFT)
  , 'r')

shortcut('directionVRT', ShortcutCategory.READER_SETTINGS, 'Direction: Vertical',
  (ctx: any) => ctx.changeReadingDir(ReadingDirection.VERTICAL),
  'v')

shortcut('toggleDoublePage', ShortcutCategory.READER_SETTINGS, 'Toggle Double Page',
  (ctx: any) => ctx.toggleDoublePages()
  , 'd')

shortcut('cycleScale', ShortcutCategory.READER_SETTINGS, 'Cycle Scale',
  (ctx: any) => ctx.cycleScale()
  , 'c')

// Menus

shortcut('toggleToolbar', ShortcutCategory.MENUS, 'Toggle Toolbar',
  (ctx: any) => {
    ctx.toolbar = !ctx.toolbar
  },
  'm')

shortcut('toggleMenu', ShortcutCategory.MENUS, 'Toggle Settings Menu',
  (ctx: any) => {
    ctx.menu = !ctx.menu
  },
  's')

shortcut('toggleExplorer', ShortcutCategory.MENUS, 'Toggle Explorer',
  (ctx: any) => {
    ctx.showThumbnailsExplorer = !ctx.showThumbnailsExplorer
  }, 't')

shortcut('escape', ShortcutCategory.MENUS, 'Close',
  (ctx: any) => {
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
  }, 'Escape')

// Make sure all shortcuts are registered before this is called
export const shortcutHelp = new MultiMap<object>()
const keyMapping = {} as Map<Shortcut>

function setupShortcuts () {
  for (const s of SHORTCUTS) {
    for (const key of s.keys) {
      keyMapping[key] = s
      shortcutHelp.add(s.category, {
        key: KEY_DISPLAY[key] || key,
        desc: s.description,
      })
    }
  }
}
setupShortcuts()

export function executeShortcut (ctx: any, e: KeyboardEvent): boolean {
  let k: string = e.key
  return keyMapping[k]?.execute(ctx)
}
