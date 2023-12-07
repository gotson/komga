export interface R2Device {
  id: string,
  name: string,
}

export interface R2Progression {
  modified: Date,
  device: R2Device,
  locator: R2Locator,
}

export interface R2Locator {
  href: string,
  type: string,
  title?: string,
  locations: R2Location,
  text?: R2LocatorText,
}

export interface R2Location {
  fragment?: string[],
  progression?: number,
  position?: number,
  totalProgression?: number,
}

export interface R2LocatorText {
  after?: string,
  before?: string,
  highlight?: string,
}
