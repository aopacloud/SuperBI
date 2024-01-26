/**
 * 请求管理器
 * 处理请求取消逻辑
 */

import { isEmpty, getPrimitiveType } from 'common/utils/object'

const pending = new Map()
const axios = require('axios')
const CancelToken = axios.CancelToken

function toString(obj) {
  if (typeof obj === 'string') return obj

  if (typeof obj === 'boolean') return obj + ''

  if (getPrimitiveType(obj) === 'object' || getPrimitiveType(obj) === 'array')
    return JSON.stringify(obj)

  return obj + ''
}

class RequestManage {
  static instance = null

  constructor() {
    this.pending = new Map()
  }

  // 以 url 和 method 来判断是否同一请求（参数除外）
  #getKey({ url, method }) {
    return url + '&' + method
  }

  // 处理请求参数
  #qsPayload(obj) {
    if (isEmpty(obj)) return ''

    const res = Object.keys(obj).reduce((pre, acc) => {
      const val = obj[acc]
      if (val !== undefined) {
        pre += '&' + acc + '=' + toString(val)
      }

      return pre
    }, '')

    return res.substring(1)
  }

  #qualPayload(config, pend) {
    const { params, data } = config
    const { params: _params, data: _data } = pend

    const curPayloadStr = [this.#qsPayload(params), this.#qsPayload(data)].join('&')
    // pend 中 data 不知道怎么转换成了json字符串 '{}'
    const prePayloadStr = [
      this.#qsPayload(_params),
      this.#qsPayload(
        _data ? (typeof _data === 'string' ? JSON.parse(_data) : _data) : undefined
      ),
    ].join('&')

    return curPayloadStr === prePayloadStr
  }

  add(config) {
    // 不需要取消的接口无需处理
    if (!config.cancellable) return

    config.cancelToken = new CancelToken(c => {
      config.cancel = c
    })
    const key = this.#getKey(config)

    if (!this.pending.has(key)) {
      this.pending.set(key, config)
    } else {
      const pend = this.pending.get(key)

      if (this.#qualPayload(config, pend)) {
        config.cancel({ cancelled: true, message: '重复请求' })
        this.pending.delete(key)
      } else {
        pend.cancel({ cancelled: true, message: '不同参数的前一请求' })
        this.pending.delete(this.#getKey(pend.url))

        this.pending.set(key, config)
      }
    }
  }

  remove(config) {
    if (!config.cancellable) return

    const key = this.#getKey(config)
    const pend = this.pending.get(key)

    if (!pend) return

    pend.cancel()
    this.pending.delete(key)
  }
}

export default new RequestManage()
