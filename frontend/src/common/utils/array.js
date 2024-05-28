import { deepClone } from './help'
import { getRandomKey } from './string'

export function flat(list = [], key = 'children') {
  if (!Array.isArray(list)) {
    throw Error(`flat require 'array' type params, please check it`)
  }

  return deepClone(list).reduce((acc, pre) => {
    const children = pre[key]
    delete pre[key]

    return acc.concat(pre, Array.isArray(children) ? flat(children, key) : [])
  }, [])
}

/**
 * 按照字段取出并扁平
 * @param {Array<T>} l
 * @param {string} k
 * @returns {Array<T>}
 */
export function pickFlatten(l = [], k = 'children') {
  if (!Array.isArray(l)) {
    throw Error(`flat require 'array' type params, please check it`)
  }

  if (!l.length) return []

  return l.reduce(
    (a, b) => a.concat(b[k] && b[k].length ? pickFlatten(b[k], k) : b),
    []
  )
}

export const flatWithOption = (
  list = [],
  parentId,
  { childrenKey = 'children', parentKey = 'parentId', idKey = 'id' }
) => {
  if (!Array.isArray(list)) {
    throw TypeError(`flat require 'array' type params, please check it`)
  }

  return deepClone(list).reduce((acc, item) => {
    const children = item[childrenKey]
    delete item[childrenKey]

    if (!Array.isArray(children)) {
      item[parentKey] = parentId
      item[idKey] = getRandomKey(6)

      return acc.concat(item)
    } else {
      item[parentKey] = parentId
      item[idKey] = getRandomKey(6)

      return acc.concat(
        item,
        flatWithOption(children, item[idKey], { childrenKey, parentKey, idKey })
      )
    }
  }, [])
}

/**
 * 深度优先查找
 * @param  {Array<T>} list 源数组
 * @param {function} cb 匹配条件
 * @return {T} 匹配项
 */
export function dfs(list = [], cb) {
  if (!list.length) return

  for (let i = 0; i < list.length; i++) {
    const item = list[i]

    if (cb(item)) return item

    if (item.children?.length) {
      const r = deepFind(item.children, cb)

      if (r) return r
    }
  }
}

/**
 * 广度优先查找
 * @param {Array<T>} list  源数组
 * @param {function} cb 匹配条件
 * @return {T} 匹配项
 */
export function bfs(list, cb) {
  if (!list.length) return

  const children = []

  for (i = 0; i < list.length; i++) {
    const item = list[i]

    if (cb(item)) return item

    if (item.children?.length) {
      children.push(...item.children)
    }
  }

  return bfs(children, cb)
}
