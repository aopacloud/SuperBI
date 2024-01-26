// export { default as AppMain } from './AppMain'
// export { default as Navbar } from './Navbar'
// export { default as Settings } from './Settings'
// export { default as TagsView } from './TagsView/index.vue'
// export { default as Breadcrumb } from './Breadcrumb/index.vue'

const allModules = import.meta.glob('./**/index.vue', { eager: true })
const modules = {}

Object.keys(allModules).forEach(key => {
  modules[key.split('/')[1]] = allModules[key].default
})

export { modules as default }
