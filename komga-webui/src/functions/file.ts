import filesize from 'filesize'

export async function getFileFromUrl(url: string, name: string = url, defaultType = 'image/jpeg') {
  const response = await fetch(url)
  const data = await response.blob()
  return new File([data], name, {
    type: data.type || defaultType,
  })
}


const filesizePartial = filesize.partial({round: 1})

export function getFileSize(n?: number): string | undefined {
  if(!n) return undefined
  return filesizePartial(n)
}
