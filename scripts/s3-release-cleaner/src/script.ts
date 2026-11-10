import {DeleteObjectsCommand, ListObjectsV2Command, S3Client} from '@aws-sdk/client-s3'

const latestVersion = process.env.LATEST_VERSION
const bucketName = process.env.B2_BUCKET_NAME
const region = process.env.B2_REGION

if (!latestVersion || !bucketName || !region) {
  console.error('Missing required env variables: LATEST_VERSION, B2_BUCKET_NAME, B2_REGION')
  process.exit(1)
}

const s3 = new S3Client({
  endpoint: `https://s3.${region}.backblazeb2.com`,
  region: region,
  credentials: {
    accessKeyId: process.env.B2_ACCESS_KEY_ID ?? '',
    secretAccessKey: process.env.B2_SECRET_ACCESS_KEY ?? '',
  },
})

async function run(): Promise<void> {
  console.log(`Scanning bucket '${bucketName}' for release version '${latestVersion}'...`)

  const allKeys: string[] = []
  let continuationToken: string | undefined

  // Handle pagination for buckets with > 1000 objects
  do {
    const res = await s3.send(
      new ListObjectsV2Command({
        Bucket: bucketName,
        ContinuationToken: continuationToken,
      })
    )

    for (const obj of res.Contents ?? []) {
      if (obj.Key) allKeys.push(obj.Key)
    }

    continuationToken = res.NextContinuationToken
  } while (continuationToken)

  const archivePrefix = `komga-${latestVersion}`
  const keysToDelete: { Key: string }[] = []

  for (const key of allKeys) {
    // 1. Purge full archives not matching the current release version
    if (key.startsWith('komga-') && (key.endsWith('.zip') || key.endsWith('.msix'))) {
      if (!key.startsWith(archivePrefix)) {
        keysToDelete.push({Key: key})
      }
    }

    // 2. Purge deltas (komga-FROM-TO-ARCH.delta) where target version (field 3) != currentVersion
    if (key.endsWith('.delta')) {
      const parts = key.split('-')
      const targetVersion = parts[2]

      if (targetVersion !== latestVersion) {
        keysToDelete.push({Key: key})
      }
    }
  }

  if (keysToDelete.length === 0) {
    console.log('No obsolete release artifacts found.')
    return
  }

  console.log(`Found ${keysToDelete.length} obsolete file(s) to remove:`)
  for (let item of keysToDelete) {
    console.log(`[DELETE] ${item.Key}`)
  }

  // Batch delete (max 1000 keys per request)
  for (let i = 0; i < keysToDelete.length; i += 1000) {
    const chunk = keysToDelete.slice(i, i + 1000)
    const deleteRes = await s3.send(
      new DeleteObjectsCommand({
        Bucket: bucketName,
        Delete: {Objects: chunk},
      })
    )

    console.log(`Deleted chunk of ${deleteRes.Deleted?.length ?? 0} file(s).`)
  }

  console.log('Cleanup completed successfully.')
}

run().catch((err) => {
  console.error('Cleanup failed:', err)
  process.exit(1)
})
