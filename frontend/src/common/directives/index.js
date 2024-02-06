const allModules = import.meta.glob('./*/index.js', { eager: true })

export default function directive(app) {
  Object.keys(allModules).forEach(key => {
    const name = key.split('/')[1]

    app.directive(name, allModules[key].default)
  })
}
