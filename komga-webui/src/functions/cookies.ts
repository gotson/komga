import { VueCookies } from 'vue-cookies'

function loadFromCookie ($cookies: VueCookies, cookieKey: string, setter: (value: any) => void): void {
  if ($cookies.isKey(cookieKey)) {
    let value = $cookies.get(cookieKey)
    setter(value)
  }
}

export type Mapper<V> = (value: any) => V

export class Cookie<V> {
  name: string
  value: V
  private _savetime = Infinity
  private $cookie: VueCookies

  private mapper: Mapper<V>

  constructor (name: string, value: V, mapper: Mapper<V>, $cookie: VueCookies) {
    this.name = name
    this.value = value
    this.mapper = mapper
    this.$cookie = $cookie
  }

  load () {
    if (this.$cookie.isKey(this.name)) {
      const v = this.$cookie.get(this.name)
      if (v) {
        this.value = this.mapper(v)
      }
    }
  }

  set (value: V, save: boolean = true) {
    if (typeof value === 'boolean' || value) {
      this.value = value
      if (save) {
        this.$cookie.set(this.name, this.value, this._savetime)
      }
    }
  }

  get (): V {
    return this.value
  }
}
