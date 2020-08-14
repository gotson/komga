export function computeCardWidth (width: number, breakpoint: string, cardPadding: number = 16): number {
  switch (breakpoint) {
    case 'xs':
      return (width - (cardPadding * 2)) / 2
    default:
      return 150
  }
}
