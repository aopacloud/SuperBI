import { createApp } from 'vue'

// global css
import '@/assets/styles/index.scss'

import App from './App.vue'
import store from './store'
import router from './router'
import directive from './directive' // directive
import plugins from './plugins' // plugins
import emittor from 'common/plugins/emittor'

import './permission' // permission control

document.title = import.meta.env.VITE_APP_TITLE

const app = createApp(App)

// 全局方法挂载
app.config.globalProperties.emittor = emittor

// 公共指令
import commonDirectives from 'common/directives'
app.use(commonDirectives)

// 注册补丁
import Patched from 'common/patched'
app.use(Patched)

app.use(router)
app.use(store)
app.use(directive)
app.use(plugins)

// 生成svg图标
import 'virtual:svg-icons-register' // 生成雪碧图
import SvgIcon from '@/components/SvgIcon/index.vue'
app.component(SvgIcon.name, SvgIcon) // 全局注册SvgIcon

// 注册vxe-table插件
import useVxeTable from './useVxeTable'
app.use(useVxeTable)

// TODO：组件内部注册失效？？？
// import VueGridLayout from 'vue-grid-layout'
// app.use(VueGridLayout)

app.mount('#app')
