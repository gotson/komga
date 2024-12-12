export function objIsEqual(o1: any, o2: any): boolean {
  return JSON.stringify(o1) === JSON.stringify(o2)
}
