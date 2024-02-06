/* UI、布局 相关 */

import { defineStore } from 'pinia'
import defaultSettings, { storagePrefix } from '@/settings'

const {
  sideTheme,
  showSettings,
  topNav: showTopNav,
  tagsView: showTagsView,
  fixedHeader: headerFixed,
  sidebarLogo: showSidebarLogo,
  dynamicTitle,
  showBreadcrumb,
} = defaultSettings

const storageUI = JSON.parse(localStorage.getItem(storagePrefix + 'ui') || '{}')

const useUIStore = defineStore('ui', {
  state: () => ({
    // 侧边栏主题
    sideTheme: storageUI.sideTheme || sideTheme,
    // 是否显示设置
    showSettings: storageUI.showSettings || showSettings,
    // 侧边栏logo
    showSidebarLogo: storageUI.showSidebarLogo || showSidebarLogo,
    // 顶栏导航
    showTopNav: storageUI.showTopNav || showTopNav,
    // 标签栏
    showTagsView: storageUI.showTagsView || showTagsView,
    showBreadcrumb: storageUI.showBreadcrumb || showBreadcrumb,
    // 固定头部
    headerFixed: storageUI.headerFixed || headerFixed,
    // 动态标题
    dynamicTitle: storageUI.dynamicTitle || dynamicTitle,
  }),
  actions: {
    // 设置侧边栏主题
    setSideTheme(theme) {
      this.sideTheme = theme
    },
    // 设置是否显示设置
    setShowSettingsShow(boolean) {
      this.showSettings = boolean
    },
    // 设置顶栏是否显示
    setTopNavShow(boolean) {
      this.showTopNav = boolean
    },
    // 设置标签栏是否显示
    setTagsViewShow(boolean) {
      this.showTagsView = boolean
    },
    // 设置UI
    setUI(key, value) {
      if (!this.hasOwnProperty(key)) return

      this[key] = value
    },
    setSiderbar(key, value) {
      if (!this.sidebar.hasOwnProperty(key)) return

      this.sidebar[key] = value
    },
  },
})

export default useUIStore
