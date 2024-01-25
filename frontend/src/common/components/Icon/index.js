import { Icon } from 'ant-design-vue'
import iconfontUrl from '@/assets/fonts/iconfont.js'

export default {
  install(Vue, option = {}) {
    const { scriptUrl = iconfontUrl, name = 'o-icon' } = option

    const IconFont = Icon.createFromIconfontCN({
      scriptUrl,
    })

    Vue.component(name, IconFont)
  },
}
