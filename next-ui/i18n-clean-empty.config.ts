import fs from 'node:fs'
import path from 'node:path'

const i18nDir = path.resolve('i18n')

if (fs.existsSync(i18nDir)) {
  const files = fs.readdirSync(i18nDir)

  for (const file of files) {
    if (!file.endsWith('.json')) continue

    const filePath = path.join(i18nDir, file)
    const stats = fs.statSync(filePath)

    // Remove 0-byte files
    if (stats.size === 0) {
      console.log(`[i18n] Removing 0-byte file: ${file}`)
      fs.unlinkSync(filePath)
      continue
    }

    // Remove files containing empty JSON objects {} or whitespace
    try {
      const content = fs.readFileSync(filePath, 'utf-8').trim()
      if (content === '' || content === '{}') {
        console.log(`[i18n] Removing empty JSON file: ${file}`)
        fs.unlinkSync(filePath)
      } else {
        const json = JSON.parse(content)
        if (Object.keys(json).length === 0) {
          console.log(`[i18n] Removing empty key object file: ${file}`)
          fs.unlinkSync(filePath)
        }
      }
    } catch {
      // If the file is completely unparseable/corrupted, remove it to prevent compiler crashes
      console.warn(`[i18n] Removing unparseable JSON file: ${file}`)
      fs.unlinkSync(filePath)
    }
  }
}
