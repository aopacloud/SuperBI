import { message } from 'ant-design-vue'
import { getToken, HEADER_TOKEN } from '@/utils/token'
import { qs } from 'common/utils/help'

const defaultHeaders = {
  'Content-Type': 'application/json',
}

const fetchFn = async (url, config) => {
  // Is need token
  const isNeedToken = config.headers?.token !== false

  if (isNeedToken) {
    const token = getToken()

    if (!token) {
      message.warning('Token is required')
      location.reload()

      return Promise.reject(new Error('Token is required'))
    } else {
      if (!config.headers) {
        config.headers = {
          ...defaultHeaders,
        }
      }
      // Add custom header [HEADER_TOKEN] with every request
      config.headers[HEADER_TOKEN] = token
    }
  }

  let urlWithBase = import.meta.env.VITE_APP_BASE_API + url

  if (config.body) {
    if (config.method === 'GET' || typeof config.method === 'undefined') {
      urlWithBase += qs.toString(config.body)
      config.body = null
    } else {
      config.body = JSON.stringify(config.body)
    }
  }

  return fetch(urlWithBase, config)
}

fetchFn.get = function (url, config) {
  return this(url, { ...config, method: 'GET' })
}

fetchFn.post = function (url, config) {
  return this(url, { ...config, method: 'POST' })
}

export default fetchFn
