type Action = (ctx: any) => void

export class Shortcut {
  description: string
  action: Action
  key: string
  display: string


  constructor (description: string, action: Action, key: string, display: string = key) {
    this.description = description
    this.action = action
    this.key = key
    this.display = display
  }

  execute (ctx: any): boolean {
    this.action(ctx)
    return true
  }
}
