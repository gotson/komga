export const delay = (ms: number) => new Promise((res) => setTimeout(res, ms))

export function base64ToFile(base64: string, filename: string, mimeType: string): File {
  // Extract the base64 string after 'data:image/...;base64,'; fall back to original if no header prefix exists
  const base64Data = base64.split(',')[1] ?? base64
  const byteString = atob(base64Data)

  const ab = new ArrayBuffer(byteString.length)
  const ia = new Uint8Array(ab)

  for (let i = 0; i < byteString.length; i++) {
    ia[i] = byteString.charCodeAt(i)
  }

  return new File([ab], filename, { type: mimeType })
}
