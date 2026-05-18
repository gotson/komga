import JSZip from 'jszip'
import {bookFileUrl} from '@/functions/urls'
import {PageDtoWithUrl} from '@/types/komga-books'

export interface WholeArchivePreloaderOpts {
  bookId: string
  supportedMediaTypes: string[]
  pages: PageDtoWithUrl[]
  getCurrentPage: () => number
  applyUrl: (idx: number, url: string) => void
  $debug?: (...args: any[]) => void
}

export class WholeArchivePreloader {
  private controller = new AbortController()
  private blobUrls: string[] = []
  private disposed = false

  constructor(private opts: WholeArchivePreloaderOpts) {}

  async run(): Promise<void> {
    const {bookId, pages, supportedMediaTypes, getCurrentPage, applyUrl, $debug} = this.opts
    try {
      const res = await fetch(bookFileUrl(bookId), {
        credentials: 'include',
        signal: this.controller.signal,
      })
      if (!res.ok) {
        $debug?.('[whole-archive-preload] /file returned', res.status)
        return
      }
      const buf = await res.arrayBuffer()
      if (this.aborted()) return
      const zip = await JSZip.loadAsync(buf)
      if (this.aborted()) return

      const cur = Math.max(0, getCurrentPage() - 1)
      const order: number[] = []
      for (let i = cur; i < pages.length; i++) order.push(i)
      for (let i = 0; i < cur; i++) order.push(i)

      for (const idx of order) {
        if (this.aborted()) return
        // 用户已经读到最后两页，剩余受益太小，主动收手
        if (getCurrentPage() >= pages.length - 1) return
        const page = pages[idx]
        if (!supportedMediaTypes.includes(page.mediaType)) continue
        const entry = zip.file(page.fileName)
        if (!entry) {
          $debug?.('[whole-archive-preload] entry missing in archive', page.fileName)
          continue
        }
        const u8 = await entry.async('uint8array')
        if (this.aborted()) return
        const blob = new Blob([u8], {type: page.mediaType})
        const url = URL.createObjectURL(blob)
        this.blobUrls.push(url)
        applyUrl(idx, url)
      }
    } catch (e: any) {
      if (e?.name !== 'AbortError') this.opts.$debug?.('[whole-archive-preload] failed', e)
    }
  }

  private aborted(): boolean {
    return this.disposed || this.controller.signal.aborted
  }

  cancel(): void {
    this.controller.abort()
  }

  dispose(): void {
    this.disposed = true
    this.controller.abort()
    this.blobUrls.forEach(URL.revokeObjectURL)
    this.blobUrls.length = 0
  }
}
