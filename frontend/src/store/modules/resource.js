import { defineStore } from 'pinia'
import { staticRoutes } from '@/router'
import { registeDynamicRoutes, transfromResourceWithRoute } from '@/router/utils'
import { listToTree, sortTree } from 'common/utils/help'
import { versionJs } from '@/versions'
import useAppStore from './app'

export default defineStore('resource', {
  state: () => ({
    // 是否已经加载
    loaded: false,
    // 当前应用资源
    resources: [],
    // 侧边栏的资源
    sidebarResources: [],
    // 顶部导航栏的资源
    navbarResources: [],
    // 所有的路由
    routes: [],
  }),
  actions: {
    setLoaded(loaded) {
      this.loaded = loaded
    },
    async fetchResource() {
      try {
        return versionJs.StoreModulesResource.fetchResourceByVersion.bind(this)()
      } catch (error) {
        console.error('资源列表获取失败', error)
      } finally {
        this.setLoaded(true)
      }
    },

    // 仅更新权限资源
    async updateResourceByWorkspaceId(workspaceId) {
      try {
        const updatePermissionResource =
          versionJs.StoreModulesResource.fetchPermissionByWorkspaceId

        return updatePermissionResource.bind(this)(workspaceId)
      } catch (error) {
        console.error('资源列表更新失败', error)
      }
    },
    // 设置路由
    setRoutes(resouces) {
      // 注册路由
      const routes = registeDynamicRoutes(resouces)
      this.routes = staticRoutes.concat(routes)
    },
    // 设置当前资源并添加路由
    setResource(list) {
      this.resources = list
      this.setRoutes(list)

      if (!list.length) {
        useAppStore().setUnAccessableInfo(
          `未开通系统权限，${versionJs.Permission.contactText}`
        )
        useAppStore().setUnAccessable(true)
      }
    },
    // 设置侧边栏资源
    setSidebarResource(list) {
      this.sidebarResources = transfromResourceWithRoute(
        sortTree(listToTree(list).sort((a, b) => a.id - b.id))
      )
    },
    // 设置导航栏资源
    setNavbarResouce(list) {
      this.navbarResources = list
    },
  },
})
