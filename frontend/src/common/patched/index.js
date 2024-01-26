const components = import.meta.glob('./components/**/index.vue', { eager: true })

export default function (app) {
  for (const path in components) {
    const v = components[path].default

    app.component(v.name, v)
  }
}
