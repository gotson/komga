export function stripAccents(s: string): string {
  return s.normalize('NFD').replace(/\p{Diacritic}/gu, '')
}
