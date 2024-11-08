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

/**
 * 一维数组去重
 * @param {array} list 源数据
 * @param {string} key 判重的字段
 * @returns {array}
 */
export function unique(list, key) {
  return list.reduce((acc, cur) => {
    const value = cur[key]

    if (acc.every(t => t[key] !== value)) {
      acc.push(cur)
    }

    return acc
  }, [])
}

/**
 * 获取数组的并集
 * @param  {any[]} res
 * @returns
 */
export function union(...res) {
  return [...new Set([].concat(...res))]
}

/**
 * 获取数据的交集
 * @param  {any[]} res
 * @returns
 */
export function intersection(...res) {
  if (!res.length || res.some(r => !r.length)) return []

  // 取出第一个作为对比
  let set0 = new Set(res[0])

  for (let i = 0; i < res.length; i++) {
    const set1 = new Set()
    const set2 = new Set(res[i])

    // 遍历当前，判断与对比的交集
    set0.forEach(v => {
      if (set2.has(v)) set1.add(v)
    })

    // 更新
    set0 = set1

    // 若交集为空，则跳出循环
    if (set0.size === 0) break
  }

  return [...set0]
}
