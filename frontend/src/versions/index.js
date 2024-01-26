const allModules = import.meta.glob('./*/index.js', { eager: true })

const jsModule = {}
const vueModule = {}
const viewsModule = {}

Object.keys(allModules).forEach(key => {
  const name = key.split('/')[1]
  const module = allModules[key]

  jsModule[name] = module['js']
  vueModule[name] = module['vue']
  viewsModule[name] = module['views']
})

const version = import.meta.env.VITE_APP_VERSION

export const versionJs = jsModule[version]

export const versionVue = vueModule[version]

export const versionViews = viewsModule[version]
