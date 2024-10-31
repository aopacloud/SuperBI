import { deepCloneByJson } from './help'

// 获取地址栏请求参数
export function getLocationQuery() {
  // 对地址栏的编码字符进行解码
  const href = decodeURIComponent(location.href)
  let res = {}

  if (href.indexOf('?') !== -1) {
    const params = href.split('?').slice(1)
    const strs = params.join('?').split('&')

    res = strs.reduce((acc, pre) => {
      const [key, ...val] = pre.split('=')
      acc[key] = val.join('=')

      return acc
    }, res)
  }

  return res
}

/**
 * 将对象转为地址栏请求 query
 * @param {object} obj
 * @returns {string}
 * @example obj2Querys({a: 1, b: 2})  => ?a=1&b=2
 */
export function obj2Querys(obj = {}) {
  return Object.keys(obj).reduce((acc, key, i) => {
    const val = obj[key]

    acc += (i !== 0 ? '&' : '?') + `${key}=${val}`
    return acc
  }, '')
}

/**
 * 将地址栏query 转为对象
 * @param {string} obj
 * @returns {object}
 * @example querys2Obj(?a=1&b=2)  => {a: 1, b: 2}
 */
export function querys2Obj(obj = {}) {
  return Object.keys(obj).reduce((acc, key, i) => {
    const val = obj[key]

    acc += (i !== 0 ? '&' : '?') + `${key}=${val}`
    return acc
  }, '')
}

/**
 * 清空 location.href query 参数
 * @param {list} querys 清空的参数列表
 */
export function clearQuerys(query = []) {
  const querys = Array.isArray(query) ? query : [query]

  if (!querys.length) {
    location.href = location.href.replace(/\?.*/, '')
  } else {
    const pathname = location.href.split('?')[0]
    const params = deepCloneByJson(getLocationQuery())
    let hasQueryChanged = false
    querys.forEach(key => {
      if (key in params) {
        delete params[key]
        hasQueryChanged = true
      }
    })

    if (hasQueryChanged) {
      const href = obj2Querys(params)
      location.href = pathname + href
    }
  }
}
