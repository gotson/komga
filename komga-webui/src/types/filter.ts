interface FiltersOptions {
  [key: string]: {
    name?: string,
    values: string[],
  },
}

interface FiltersActive {
  [key: string]: string[],
}
