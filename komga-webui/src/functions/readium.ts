import {Locator, ReadingPosition} from '@d-i-t-a/reader'
import {R2Location, R2Locator, R2Progression} from '@/types/readium'
import {Locations} from '@d-i-t-a/reader/dist/types/model/Locator'
import urls from '@/functions/urls'

export function createR2Progression(locator: Locator): R2Progression {
  return {
    modified: new Date(),
    device: {
      name: 'Komga-webui',
      id: 'unused',
    },
    locator: locatorToR2Locator(locator),
  }
}

function locatorToR2Locator(locator: Locator): R2Locator {
  return {
    href: locator.href.startsWith(urls.originNoSlash) ? locator.href.replace(/(.*\/resource\/)/, '') : locator.href,
    type: locator.type || 'application/octet-stream',
    title: locator.title,
    locations: locationsToR2Location(locator.locations),
    text: locator.text,
  }
}

function locationsToR2Location(location: Locations): R2Location {
  return {
    fragment: location.fragment ? [location.fragment] : [],
    progression: location.progression,
    position: location.position,
    totalProgression: location.totalProgression,
  }
}

export function r2ProgressionToReadingPosition(progression?: R2Progression, bookId: string): ReadingPosition | undefined {
  try {
    return {
      created: progression.modified,
      href: `${urls.originNoSlash}/api/v1/books/${bookId}/resource/${progression.locator.href}`,
      type: progression.locator.type,
      title: progression.locator.title,
      locations: {
        fragment: progression.locator.locations.fragment ? progression.locator.locations.fragment[0] : undefined,
        position: progression.locator.locations.position,
        progression: progression.locator.locations.progression,
        totalProgression: progression.locator.locations.totalProgression,
      },
      text: progression.locator.text,
    }
  } catch (e) {
    return undefined
  }
}
