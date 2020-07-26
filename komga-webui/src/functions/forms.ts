
export interface SelectItem<V> {
  text: string
  value: V
}

export interface Map<V> {
  [key: string]: V
}

export class MultiMap<V> {
  dict: Map<V[]> = {}

  add (key: string, value: V) {
    this.dict[key] = (this.dict[key]?.concat([value])) || [value]
  }

  get (key: string): V[] {
    return this.dict[key]
  }

  items () {
    return Object.keys(this.dict).map((k) => ({ key: k, value: this.dict[k] }))
  }
}
