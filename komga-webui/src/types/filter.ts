export interface FiltersOptions {
  [key: string]: {
    name?: string,
    values?: NameValue[],
    search?: (search: string) => Promise<string[]>,
    anyAllSelector?: boolean,
  },
}

export interface NameValue {
  name: string,
  value: any,
  // an optional negative value
  nValue?: any,
}

export interface FiltersActive {
  [key: string]: any[],
}

export interface FiltersActiveMode {
  [key: string]: FilterMode,
}

export interface FilterMode {
  allOf: boolean,
}

export const FILTER_ANY = 'KOMGA____ANY____'
export const FILTER_NONE = 'KOMGA____NONE____'
