export class PageLoader<T> {
  private readonly pageable: PageRequest
  private readonly loader: (pageRequest: PageRequest) => Promise<Page<T>>

  private currentPage = undefined as unknown as Page<T>
  private loadedPages: number[] = []
  public readonly items: T[] = []

  get page() {
    return this.currentPage?.number || 0
  }
  get hasNextPage() {
    return !this.currentPage ? false : !this.currentPage.last
  }

  constructor(pageable: PageRequest, loader: (pageRequest: PageRequest) => Promise<Page<T>>) {
    this.pageable = pageable
    this.loader = loader
  }

  async loadNext(): Promise<boolean> {
    // load first page if nothing has been loaded yet
    if (!this.currentPage) {
      this.loadedPages.push(this.pageable.page || 0)
      this.currentPage = await this.loader(this.pageable)
      this.items.push(...this.currentPage.content)
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
        this.items.push(...this.currentPage.content)
        return true
      }
      return false
    }
  }
}
