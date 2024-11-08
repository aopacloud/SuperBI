import { defineStore } from 'pinia'
import { setDocumentTitle } from '@/utils'
import { storagePrefix, defaultActiveTimeOffset } from '@/settings'
import { versionJs } from '@/versions'
import useResourceStore from './resource'

const initialWorkspaceId =
  sessionStorage.getItem(storagePrefix + 'workspaceId') ||
  localStorage.getItem(storagePrefix + 'workspaceId') ||
  undefined

const useAppStore = defineStore('app', {
  state: () => ({
    // 应用信息
    info: {},
    // 侧边栏
    sidebar: {
      // 显示、隐藏
      hide: versionJs.StoreModulesApp.siderbarHide
    },

    // 页面标题
    title: '',

    // 所有空间列表
    workspaceList: [],
    // 当前空间ID
    workspaceId: +initialWorkspaceId,

    // 当前空间详情
    workspaceItem: {},

    // 不可访问状态
    unAccessable: false,
    // 不可访问状态信息
    unAccessableInfo: {},

    // 应用入口
    entryInfo: {
      origin: '', // 来源名
      pToken: '' // 代理token
    },

    layout: {}
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
    entryOrigin: state => state.entryInfo.origin,
    entryToken: state => state.entryInfo.pToken,
    appLayout: state => state.layout || {}
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
          status
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
        this.setUnAccessableInfo(
          `未开通系统权限，${versionJs.Permission.contactText}`
        )
        this.setUnAccessable(true)
      } else {
        // 无当前（缓存）空间访问权限
        if (
          +initialWorkspaceId &&
          !list.some(item => item.id === +initialWorkspaceId)
        ) {
          this.setUnAccessableInfo(
            `未开通当前空间的访问权限，${versionJs.Permission.contactText}`
          )
          this.setUnAccessable(true)
        }
      }
    },

    // 设置当前空间ID
    async setWorkspaceId(id, updateResource) {
      this.workspaceId = id

      localStorage.setItem(storagePrefix + 'workspaceId', id)
      sessionStorage.setItem(storagePrefix + 'workspaceId', id)

      if (updateResource) {
        await useResourceStore().updateResourceByWorkspaceId(id)
      }
    },

    // 设置当前空间
    setWorkspaceItem(info = {}) {
      this.workspaceItem = info

      this.setWorkspaceId(info.id)
    },

    // 设置入口信息
    setEntryInfo(info = {}) {
      this.entryInfo = info
    },

    setLayout(k, v) {
      this.layout[k] = v
    }
  }
})

export default useAppStore
