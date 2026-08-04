const themes = ['light', 'dark']
const viewports = [
  { name: 'mobile', value: 'xs' },
  { name: 'desktop', value: 'md' },
]

const allModes: Record<string, object> = {}

// viewport / theme combinations
viewports.forEach((vp) => {
  themes.forEach((t) => {
    allModes[`${t} ${vp.name}`] = {
      viewport: vp.value,
      theme: t,
    }
  })
})

export { allModes }
