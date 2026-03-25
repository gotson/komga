import { partial } from 'filesize'
import { currentLocale } from '@/utils/i18n/locale-helper'

export const delay = (ms: number) => new Promise((res) => setTimeout(res, ms))

// Query Intl for the unit symbol of a given unit (byte, kilobyte, etc.)
function getUnitSymbol(
  locale: string,
  unit: Intl.NumberFormatOptions['unit'],
  unitDisplay: Intl.NumberFormatOptions['unitDisplay'] = 'narrow',
): string {
  const formatter = new Intl.NumberFormat(locale, {
    style: 'unit',
    unit: unit,
    unitDisplay: unitDisplay,
  })
  return formatter.formatToParts(1).find((it) => it.type === 'unit')!.value
}

// Map the units we care about to symbols for a given locale
function getByteSymbolsForLocale(locale: string): Record<string, string> {
  return {
    B: getUnitSymbol(locale, 'byte', 'narrow'),
    kB: getUnitSymbol(locale, 'kilobyte', 'narrow'),
    MB: getUnitSymbol(locale, 'megabyte', 'narrow'),
    GB: getUnitSymbol(locale, 'gigabyte', 'narrow'),
    TB: getUnitSymbol(locale, 'terabyte', 'narrow'),
    PB: getUnitSymbol(locale, 'petabyte', 'narrow'),
  }
}

const filesizePartial = partial({
  round: 1,
  locale: currentLocale,
  symbols: getByteSymbolsForLocale(currentLocale),
})

export function getFileSize(n?: number): string | undefined {
  if (!n) return undefined
  return filesizePartial(n)
}
