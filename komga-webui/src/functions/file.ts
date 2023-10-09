import {partial} from 'filesize'

export async function getFileFromUrl(url: string, name: string = url, defaultType = 'image/jpeg', fetchOptions = {}) {
  const response = await fetch(url, fetchOptions)
  const data = await response.blob()
  return new File([data], name, {
    type: data.type || defaultType,
  })
}


const filesizePartial = partial({round: 1})

export function getFileSize(n?: number): string | undefined {
  if(!n) return undefined
  return filesizePartial(n)
}
