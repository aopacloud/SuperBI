const allModules = import.meta.glob('./**/*.vue', { eager: true })

/**
 *
 * {
 *  './a/a.vue': [Module: vueExport],
 *  './a/b.vue': [Module: vueExport]
 * }
 *
 * => { 'aA': [Module: vueExport], 'aB': [Module: vueExport] }
 */

const upcaseFirst = str => {
  const [r, ...res] = str

  return r.toUpperCase() + res.join('')
}

const modules = Object.keys(allModules).reduce((acc, key) => {
  const name = key
    .split('/')
    .slice(1)
    .join('.')
    .split('.')
    .slice(0, -1)
    .map(t => upcaseFirst(t))
    .join('')
    .replace(/(Index)$/, '')

  acc[name] = allModules[key].default

  return acc
}, {})

export default modules
