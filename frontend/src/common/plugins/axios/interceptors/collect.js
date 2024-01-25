/**
 * 收集拦截器
 * 1. 收集请求发送到响应时间(包含网络时间)
 * TODO: ？？？
 */

export default function () {
  this.interceptors.request.use(config => {
    if (!config.metadata) config.metadata = {}

    config.metadata = {
      startTime: Date.now()
    }

    return config
  })

  this.interceptors.response.use(response => {
    response.config.metadata.endTime = Date.now()
    const { startTime, endTime } = response.config.metadata
    response.config.metadata.responseTime = endTime - startTime

    return response
  })
}
