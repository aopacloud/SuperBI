import { removeToken } from '@/utils/token'

// 处理 axios 响应错误
export function handleResponse(error = {}, log = console.error) {
  const { code, config, response, request, message } = error

  if (response) {
    // 请求成功发出且服务器也响应了状态码，但状态代码超出了 2xx 的范围
    const {
      status,
      statusText,
      data: { error: err, message },
    } = response

    // 401 Unauthorized（未授权页面刷新，触发授权登录页）
    if (status === 401 && process.env.NODE_ENV !== 'development') {
      removeToken().then(() => location.reload())
    }

    const errorMsg = message || err || statusText
    // 500 Internal Server Error; 501 Not Implemented; 502 Bad Gateway; 503 Service Unavailable; 504 Gateway Timeout
    if (status >= 500) {
      log(errorMsg || '服务器错误，请联系管理员')
    } else if (status >= 400) {
      // 400 Bad Request; 403 Forbidden; 404 Not Found; 405 Method Not Allowed;
      // 406 Not Acceptable; 407 Proxy Authentication Required; 408 Request Timeout; 409 Conflict
      log(errorMsg || '请求响应错误，请检查')
    } else {
      errorMsg && log(errorMsg)
    }
  } else if (request) {
    // 请求已经成功发起，但没有收到响应
    log('请求响应错误')
  } else {
    // 重复请求的提示不打印
    if (error.cancelled) return

    // 发送请求时出了点问题
    log(message || '请求发送错误')
  }
}
