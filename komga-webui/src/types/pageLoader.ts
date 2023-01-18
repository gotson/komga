export class PageLoader<T> {
  private readonly pageable: PageRequest
  private readonly loader: (pageRequest: PageRequest) => Promise<Page<T>>
  private readonly postProcessor: (item: T) => T

  private currentPage = undefined as unknown as Page<T>
  private loadedPages: number[] = []
  private _tick = 0
  public readonly items: T[] = []

  get hasNextPage() {
    return !this.currentPage ? false : !this.currentPage.last
  }

  get tick() {
    return this._tick
  }

  // whether anything has been loaded yet
  get hasLoadedAny() {
    return this.currentPage
  }

  constructor(pageable: PageRequest, loader: (pageRequest: PageRequest) => Promise<Page<T>>, postProcessor: (item: T) => T = (item) => item) {
    this.pageable = pageable
    this.loader = loader
    this.postProcessor = postProcessor
  }

  async reload() {
    if (!this.currentPage) return this.loadNext()

    const pageable = Object.assign({}, this.pageable,
      {
        size: (this.currentPage.number + 1) * (this.pageable.size || 20),
        page: 0,
      })
    const page = await this.loader(pageable)
    this.items.splice(0, this.items.length, ...page.content.map(this.postProcessor))
    this._tick++
  }

  async loadNext(): Promise<boolean> {
    // load first page if nothing has been loaded yet
    if (!this.currentPage) {
      this.loadedPages.push(this.pageable.page || 0)
      this.currentPage = await this.loader(this.pageable)
      this.items.push(...this.currentPage.content.map(this.postProcessor))
      this._tick++
      return true
    }
    // if the last page has been loaded, do nothing
    else if (this.currentPage.last) return false
    else {
      const nextPage = this.currentPage.number + 1
      if (!this.loadedPages.includes(nextPage)) {
        this.loadedPages.push(nextPage)
        const pageable = Object.assign({}, this.pageable, {page: nextPage})
        this.currentPage = await this.loader(pageable)
        this.items.push(...this.currentPage.content.map(this.postProcessor))
        this._tick++
        return true
      }
      return false
    }
  }
}
