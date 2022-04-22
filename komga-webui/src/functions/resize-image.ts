function getCanvasBlob(canvas: HTMLCanvasElement): Promise<Blob | null> {
  return new Promise((resolve) => {
    canvas.toBlob((blob: Blob | null) => { resolve(blob) })
  })
}

export async function resizeImageFile(imageFile: File, maxWidth: number = 500, maxHeight: number = 500): Promise<File> {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')
  if (ctx !== null) {
    const img = new Image()
    img.src = URL.createObjectURL(imageFile)
    await img.decode()

    let width = img.width
    let height = img.height

    // Calculate width and height of the smaller image
    if (width > height) {
      if (width > maxWidth) {
        height = height * (maxWidth / width)
        width = maxWidth
      }
    } else {
      if (height > maxHeight) {
        width = width * (maxHeight / height)
        height = maxHeight
      }
    }

    canvas.width = width
    canvas.height = height
    // Release blob data to save browser memory
    URL.revokeObjectURL(img.src)
    ctx.drawImage(img, 0, 0, width, height)
    const blob = await getCanvasBlob(canvas)
    if (blob !== null) {
      return new File([blob], 'poster', {lastModified: Date.now()})
    }
  }
  // If we have not returned before here, resizing has failed for some reason.
  // Maybe the browser doesn't support 2D canvas or image loading was not possible.
  // Just return the original image.
  return imageFile
}
