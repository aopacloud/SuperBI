/**
 * 基础的拦截器
 * 1. 请求拦截器，请求前添加 token 请求头
 * 2. 请求 admin 接口，无需拼接服务前缀
 */

import store from '@/store'
import Cookies from 'js-cookie'
import RequestManage from '../manage.js'

let _config = null // 请求的备份，请求失败时，从管理器中移除请求

export default function () {
  this.interceptors.request.use(config => {
    // 配置token等请求头信息
    const token = Cookies.get('weauth_token')
    if (token) {
      config.headers.Authorization = token
    }
    // 请求 admin 接口不需要添加服务前缀
    if (config.url.startsWith('/admin')) {
      const { requestPrefix } = store.getters['appConfig']
      config.baseURL = config.baseURL.replace(requestPrefix, '')
    }

    RequestManage.add(config)

    _config = config

    return config
  })

  this.interceptors.response.use(
    response => {
      const config = response.config

      RequestManage.remove({
        ...config,
        data: config.data
          ? typeof config.data === 'string'
            ? JSON.parse(config.data)
            : config.data
          : undefined
      })

      _config = null

      return response
    },
    error => {
      // 常规请求错误，从管理中中移除
      if (!error.message.cancelled && _config) {
        RequestManage.remove({
          ..._config,
          data: _config.data
        })
      }

      if (error.message?.cancelled) {
        return Promise.reject({
          cancelled: true,
          message: error.message.message
        })
      } else {
        return Promise.reject(error)
      }
    }
  )
}
