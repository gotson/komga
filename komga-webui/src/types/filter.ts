interface FiltersOptions {
  [key: string]: {
    name?: string,
    values?: NameValue[],
    search?: (search: string) => Promise<string[]>,
    anyAllSelector?: boolean,
  },
}

interface NameValue {
  name: string,
  value: any,
  // an optional negative value
  nValue?: any,
}

interface FiltersActive {
  [key: string]: any[],
}

interface FiltersActiveMode {
  [key: string]: FilterMode,
}

interface FilterMode {
  allOf: boolean,
}
