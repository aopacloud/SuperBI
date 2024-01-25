import { defineStore } from 'pinia'
import { message } from 'ant-design-vue'
import { login, getInfo } from '@/apis/login'
import { getToken, setToken, removeToken } from '@/utils/token'
import { storagePrefix } from '@/settings'
import defAva from '@/assets/images/avatar.svg'
import useAppStore from './app'

const userinfoStorage = JSON.parse(
  localStorage.getItem(storagePrefix + 'userinfo') || '{}'
)

const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: userinfoStorage,
    token: getToken(),
    permissionCodes: [],
    roles: [],
  }),
  getters: {
    // 超级管理员账户
    isSuperAdmin: state => state.userInfo.isSuperAdmin,
    username: state => state.userInfo.username,
    avatar: state => {
      const { avatar } = state.userInfo

      if (!avatar) return defAva

      return avatar.startsWith('http') ? avatar : 'data:image/gif;base64,' + avatar
    },
  },
  actions: {
    setToken(token) {
      setToken(token)
      this.token = token
    },
    // 设置用户信息
    setUserinfo(info = '') {
      this.userInfo = info

      localStorage.setItem(storagePrefix + 'userinfo', JSON.stringify(info))
    },
    clearUserinfo() {
      this.userInfo = {}
      localStorage.removeItem(storagePrefix + 'userinfo')
    },
    // 设置权限code
    setPermissionCodes(codes = []) {
      this.permissionCodes = codes
    },
    // 是否有权限
    hasPermission(code, and) {
      if (this.isSuperAdmin) return true

      if (typeof code === 'undefined') return false

      const codes = Array.isArray(code) ? code : [code]

      return and
        ? codes.every(cd => this.permissionCodes.includes(cd))
        : codes.some(cd => this.permissionCodes.includes(cd))
    },
    // 设置角色
    setRoles(roles = []) {
      this.roles = roles
    },

    // 登录
    login({ username, password, code, uuid }) {
      return new Promise((resolve, reject) => {
        login({ username, password, code, uuid })
          .then(res => {
            message.success('登录成功')

            this.setToken(res.token)

            resolve()
          })
          .catch(error => {
            reject(error)
          })
      })
    },
    // 获取用户信息
    getInfo() {
      return new Promise((resolve, reject) => {
        getInfo()
          .then(user => {
            this.setUserinfo(user)
            this.setRoles(user.isSuperAdmin ? ['SuperAdmin'] : [])

            useAppStore().setWorkspaceList(user.workspaces)

            if (!user.workspaces.length) {
              reject(user)
            }

            if (!useAppStore().workspaceId) {
              useAppStore().setWorkspaceItem(user.workspaces[0])
            } else {
              let item = user.workspaces.find(t => t.id === useAppStore().workspaceId)
              // 没有则取默认第一个有权限的空间
              if (!item) item = user.workspaces[0]

              useAppStore().setWorkspaceItem(item)
            }

            resolve(user)
          })
          .catch(error => {
            console.error('error', error)
            reject(error)
          })
      })
    },
    // 退出系统
    logout() {
      return new Promise(resolve => {
        this.clearUserinfo()
        removeToken()

        message.success('退出登录')

        setTimeout(resolve)
      })
    },
  },
})

export default useUserStore
