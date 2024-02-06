import { defineStore } from 'pinia'
import { setDocumentTitle } from '@/utils'
import { storagePrefix, defaultActiveTimeOffset } from '@/settings'
import { versionJs } from '@/versions'

const useAppStore = defineStore('app', {
  state: () => ({
    // 应用信息
    info: {},
    // 侧边栏
    sidebar: {
      // 显示、隐藏
      hide: versionJs.StoreModulesApp.siderbarHide,
    },

    // 页面标题
    title: '',

    // 所有空间列表
    workspaceList: [],
    // 当前空间ID
    workspaceId: +localStorage.getItem(storagePrefix + 'workspaceId') ?? undefined,
    // 当前空间详情
    workspaceItem: {},

    // 不可访问状态
    unAccessable: false,
    // 不可访问状态信息
    unAccessableInfo: {},
  }),
  getters: {
    // 时区便宜设置
    timeOffsets: state => {
      const { selectedTimezone = '' } = state.workspaceItem.config || {}

      return selectedTimezone.split(',').sort((a, b) => a - b)
    },
    // 当前时区
    activeTimeOffset: state => {
      const { activeTimezone } = state.workspaceItem.config || {}

      return activeTimezone || defaultActiveTimeOffset
    },
  },
  actions: {
    setInfo(info) {
      this.info = info
    },
    // 设置不可访问
    setUnAccessable(unaccessable) {
      this.unAccessable = unaccessable
    },
    setUnAccessableInfo(info, status = 403) {
      if (this.unAccessable) return

      if (typeof info === 'string') {
        this.unAccessableInfo = {
          title: info,
          status,
        }
      } else {
        this.unaccessableInfo = { status, ...info }
      }
    },

    // 切换侧边栏显示状态
    toggleSideBarHide(status) {
      this.sidebar.hide = status
    },

    // 设置网页标题
    setTitle(title) {
      this.title = title
      setDocumentTitle()
    },

    // 设置所有空间列表
    setWorkspaceList(list = []) {
      this.workspaceList = list

      // 无任何空间访问权限
      if (!list.length) {
        this.setUnAccessableInfo(`未开通系统权限，${versionJs.Permission.contactText}`)
        this.setUnAccessable(true)
      } else {
        // 无当前（缓存）空间访问权限
        if (this.workspaceId && !list.some(item => item.id === this.workspaceId)) {
          this.setUnAccessableInfo(
            `未开通当前空间的访问权限，${versionJs.Permission.contactText}`
          )
          this.setUnAccessable(true)
        }
      }
    },

    // 设置当前空间ID
    setWorkspaceId(id) {
      this.workspaceId = id

      localStorage.setItem(storagePrefix + 'workspaceId', id)
    },

    // 设置当前空间
    setWorkspaceItem(info = {}) {
      this.workspaceItem = info

      this.setWorkspaceId(info.id)
    },
  },
})

export default useAppStore
