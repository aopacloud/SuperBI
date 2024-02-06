const allModules = import.meta.glob('./**.js(x)?', { eager: true })

/**
 *
 * {
 *  './a/a.js': [Module: jsExport],
 *  './a/b.js': [Module:jsExport]
 * }
 *
 * => { 'AA': [Module: jsExport], 'AB': [Module: jsExport] }
 */

const upcaseFirst = str => {
  const [r, ...res] = str

  return r.toUpperCase() + res.join('')
}

const modules = Object.keys(allModules).reduce((acc, key) => {
  const name = key
    .split('/')[1]
    .split('.')
    .slice(0, -1)
    .map(t => upcaseFirst(t))
    .join('')

  acc[name] = allModules[key]

  return acc
}, {})

export default modules
