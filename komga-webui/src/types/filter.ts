interface FiltersOptions {
  [key: string]: {
    name?: string,
    values: NameValue[],
  },
}

interface NameValue {
  name: string,
  value: string,
}

interface FiltersActive {
  [key: string]: string[],
}
