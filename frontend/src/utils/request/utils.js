import { message } from 'ant-design-vue'
import { removeToken } from '@/utils/token'
import useAppStore from '@/store/modules/app'
import useUserStore from '@/store/modules/user'

export const errorCode = {
  401: '认证失败，无法访问系统资源',
  403: '无访问权限，请联系对应资源的负责人',
  404: '访问资源不存在',
  500: '服务器错误，请反馈给管理员',
  601: '友好的提示',
  default: '系统未知错误，请反馈给管理员',
}

export const handleResponse = error => {
  const { statusCode, message: msg } = error

  // Error message
  const errorMsg = msg || errorCode[statusCode] || errorCode['default']

  // UnAccess for resource
  if (statusCode === 403) {
    useAppStore().setUnAccessableInfo(errorMsg)
    useAppStore().setUnAccessable(true)

    return Promise.reject(error)
  }

  // Helpful error message
  if (statusCode === 601) {
    message.error(errorMsg)

    return Promise.reject(error)
  }

  // Service backend error
  if (statusCode === 500) {
    message.error(errorMsg)

    return Promise.reject(error)
  }

  return Promise.reject(error)
}

export const handleResponseError = error => {
  const { message: errorMsg, code: errorCode, response, config } = error
  const msgPrefix = errorCode + ': '

  if (response.status === 401) {
    useUserStore()
      .logout()
      .then(() => {
        message.error(msgPrefix + errorCode[response.status])

        removeToken().then(() => location.reload())
      })

    return Promise.reject(error)
  }

  // Not show erroMessage
  if (config.hideErrorMessage) return Promise.reject(error)

  // Network error
  if (errorMsg === 'Network Error') {
    message.error(msgPrefix + '网络连接失败  请刷新重试')
  } else if (errorMsg.includes('timeout')) {
    // Network timeout
    message.error(msgPrefix + '接口请求超时')
  } else {
    // Other error
    message.error(msgPrefix + errorMsg)
  }

  return Promise.reject(error)
}
