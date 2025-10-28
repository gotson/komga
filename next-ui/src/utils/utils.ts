import { partial } from 'filesize'

export const delay = (ms: number) => new Promise((res) => setTimeout(res, ms))

const filesizePartial = partial({ round: 1 })
export function getFileSize(n?: number): string | undefined {
  if (!n) return undefined
  return filesizePartial(n)
}
