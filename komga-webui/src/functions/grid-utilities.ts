export function computeCardWidth (width: number, breakpoint: string, cardPadding: number = 16): number {
  switch (breakpoint) {
    case 'xs':
      return (width - (cardPadding * 2)) / 2
    case 'sm':
      return (width - (cardPadding * 3)) / 3
    default:
      return 150
  }
}
