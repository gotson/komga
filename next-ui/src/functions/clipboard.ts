export async function extractImageFromClipboard(item: ClipboardItem | DataTransferItem) {
  if (item instanceof ClipboardItem) return extractImageFromClipboardItem(item)
  if (item instanceof DataTransferItem) return extractImageFromDataTransferItem(item)
}

async function extractImageFromDataTransferItem(item: DataTransferItem): Promise<File | undefined> {
  // raw image
  if (item.kind === 'file' && item.type.startsWith('image/')) {
    return item.getAsFile() ?? undefined
  }

  // data URI
  if (item.kind === 'string' && item.type === 'text/plain') {
    const text = await new Promise<string>((resolve) => item.getAsString(resolve))
    return extractImageFromDataUri(text)
  }
}

async function extractImageFromClipboardItem(item: ClipboardItem): Promise<File | undefined> {
  // raw image
  const imageType = item.types.find((type) => type.startsWith('image/'))
  if (imageType) {
    const blob = await item.getType(imageType)
    const extension = imageType.split('/')[1] ?? 'unknown'
    return new File([blob], `pasted-image-${Date.now()}.${extension}`, { type: imageType })
  }

  // data URI
  if (item.types.includes('text/plain')) {
    const blob = await item.getType('text/plain')
    const text = await blob.text()

    return extractImageFromDataUri(text)
  }
}

async function extractImageFromDataUri(text: string): Promise<File | undefined> {
  const trimmedText = text.trim()
  if (trimmedText.startsWith('data:image/')) {
    try {
      const res = await fetch(trimmedText)
      const imageBlob = await res.blob()
      const mimeType = imageBlob.type
      const extension = mimeType.split('/')[1] ?? 'unknown'

      return new File([imageBlob], `pasted-image-${Date.now()}.${extension}`, { type: mimeType })
    } catch (ignore) {}
  }
}
