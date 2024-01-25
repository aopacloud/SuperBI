import axios from 'axios'
import { message } from 'ant-design-vue'
import { getToken, HEADER_TOKEN, removeToken } from '@/utils/token'
import { handleResponse, handleResponseError } from './utils'
import requestWorkspaceIdInterceptor from './workspaceIdInterceptor'
import useUserStore from '@/store/modules/user'

axios.defaults.headers['Content-Type'] = 'application/json;charset=utf-8'

// Create axios instance
const service = axios.create({
  // Request baseUrl
  baseURL: import.meta.env.VITE_APP_BASE_API,
  // Timeout
  timeout: 1000 * 60,
})

// Add custom interceptor to deal requestParams or requestBody by add workspaceId
requestWorkspaceIdInterceptor.call(service)

// Request interceptor
service.interceptors.request.use(
  config => {
    // Is need token
    const isNeedToken = config.headers.token !== false

    if (isNeedToken) {
      const token = getToken()

      if (!token) {
        message.warning('Token is required')
        location.reload()

        return Promise.reject(new Error('Token is required'))
      } else {
        // Add custom header [HEADER_TOKEN] with every request
        config.headers[HEADER_TOKEN] = token
      }
    }

    return config
  },
  error => {
    console.error('Request error', error)

    return Promise.reject(error)
  }
)

// Response interceptor
service.interceptors.response.use(
  res => {
    const { status: httpCode, statusText, data, request, config } = res
    // Http code error
    if (httpCode !== 200) {
      message.error('响应异常 ' + httpCode + ' ' + statusText)

      return Promise.reject(res)
    }

    // ThirdApi returns directly
    if (config.thirdParty) return data

    // ResponseType is 'blob' or 'arraybuffer'
    if (request.responseType === 'blob' || request.responseType === 'arraybuffer') {
      // returns unexpected type with application/json
      if (data.type === 'application/json') {
        return data.text().then(r => handleResponse(JSON.parse(r)))
      } else {
        return data
      }
    }

    // Login status expired
    if (httpCode === 401 || data.statusCode === 401) {
      useUserStore()
        .logout()
        .then(() => {
          location.reload()
        })

      return Promise.reject('无效的会话，或者会话已过期，请重新登录。')
    }

    // Success
    if (data.statusCode === 200) {
      return data.data
    }

    // Not show erroMessage
    if (config.hideErrorMessage) {
      return Promise.reject(res)
    }

    return handleResponse(data)
  },
  error => {
    return handleResponseError(error)
  }
)

export default service

// 轮询
export const useLoopRequest = (requestConfig, interval = 10000) => {
  let timer = null

  const request = () => service(requestConfig)

  const stop = () => {
    clearInterval(timer)

    timer = null
  }

  const start = () => {
    return new Promise((resolve, reject) => {
      timer = setTimeout(() => {
        request()
          .then(res => {
            start()
            resolve(res)
          })
          .catch(reject)
      }, interval)
    })
  }

  return {
    run: () => request(),
    start,
    stop,
  }
}
