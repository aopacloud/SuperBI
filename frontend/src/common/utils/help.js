﻿/****** 工具函数 ******/

/**
 * 等待睡眠
 * @param {number} ms 延时时间
 * @returns
 */
export function sleep(ms = 0) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

export function sleepSync(ms = 0) {
  const start = Date.now()
  while (Date.now() - start < ms) {}
}

/**
 * 生成随机key
 * @param  {number} len 长度
 * @return {string}
 * @example
 *  getRandomKey(5) => 97214
 */
export function getRandomKey(len = 10) {
  if (len > 14) len = 14

  return String(Math.random()).substring(2, len + 2)
}

/**
 * 生成 uuid
 * @returns {string}
 * @example
 *  getRandomKey(5) => ff929928-717b-4445-a117-a94a483a5cbe
 */
/* export  */ function uuid() {
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    var r = (Math.random() * 16) | 0,
      v = c == 'x' ? r : (r & 0x3) | 0x8
    return v.toString(16)
  })
}

/**
 * 数字补零
 * @param {number} 需要补全的数字
 * @param {number} 补全长度
 * @returns {string}
 * @example
 *  pad0(2, 3) => '002'
 */
export function pad0(number, len = 2) {
  return String(number).padStart(len, '0')
}

/**
 * 深克隆
 * @param {T} obj
 * @returns {T}
 * @example
 *  deepClone({ a: 1 }) => { a: 1 }
 */
export function deepClone(obj) {
  const type = Object.prototype.toString.call(obj).slice(8, -1).toLowerCase()

  if (['string', 'number', 'boolean'].includes(type)) return obj
  if (type === 'date') return new Date(obj)
  if (type === 'regexp') return new RegExp(obj)
  if (type === 'function') return new Function('return ' + obj.toString())()
  if (type === 'array') return obj.map(t => deepClone(t))

  if (type === 'object') {
    const res = {}
    for (const k in obj) {
      res[k] = deepClone(obj[k])
    }

    return res
  }
}

// 深克隆 - JSON
export function deepCloneByJson(obj) {
  return JSON.parse(JSON.stringify(obj))
}

/**
 * 扁平化数组
 * @param  {array} list  源数组
 * @param  {string} key 扁平化的key
 * @return {array}    移除递归key的扁平化数组
 * @example
 *  flat() => [ { id: 1 }, { id: 2 }, { id: 3 } ]
 */
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
 * 深度优先查找
 * @param  {array<T>} list 源数组
 * @param  {string} key    查找的key
 * @param  {string} val    匹配的value
 * @return {T}      匹配项
 */
export function deepFind(list = [], condition) {
  for (let i = 0; i < list.length; i++) {
    const item = list[i]

    if (condition(item)) {
      return item
    } else {
      const { children = [] } = item

      if (children.length) {
        const _item = deepFind(children, condition)

        if (_item) {
          return _item
        }
      }
    }
  }
}

/**
 * 查找数组中符合条件的项
 * @param {Array<T>} list 数据源
 * @param {() => boolean} condition 条件
 * @param {() => void} cb 回调函数
 * @returns {T} 符合条件的数组项
 * @example
 *  findBy([{ id: 1 }, { id: 2 }], item => item.id === 1, item => item.id => 1)
 */
export function findBy(list = [], condition, cb) {
  const item = list.find(condition)

  return typeof cb === 'function' ? cb(item) : item
}

/**
 * 【异步】查找数组中符合条件的项
 * @param {Array<T>} list 数据源
 * @param {() => boolean} condition 条件
 * @returns {T} 返回promise对象
 * @example
 *  findByAync([{ id: 1 }, { id: 2 }], item => item.id === 1)
 */
export function findByAync(list = [], condition) {
  const item = list.find(condition)

  return item ? Promise.resolve(item) : Promise.reject()
}

/**
 * 防抖
 * @param {T} fn 执行函数
 * @param {number} delay 延时执行时间
 * @param {boolean} immediate 是否立即执行
 * @returns {T}
 * @example const fnDebounced =debounce(fn, 500, true), fnDebounced()
 */
export function debounce(fn, delay = 500, immediate = false) {
  let timer = null,
    date = Date.now()

  return function (...args) {
    if (immediate) {
      if (Date.now() - date < delay) {
        date = Date.now()

        return
      }

      date = Date.now()
      fn.apply(this, args)
    } else {
      clearTimeout(timer)

      timer = setTimeout(() => {
        fn.apply(this, args)
      }, delay)
    }
  }
}

/**
 * 节流
 * @param {T} fn 执行函数
 * @param {number} delay 延时执行时间
 * @returns {T}
 * @example
 *  const fnThrottled =throttle(fn, 500), fnThrottled()
 */
export function throttle(fn, ms = 500) {
  let pre = 0

  return function (...args) {
    const cur = new Date()
    if (cur - pre > ms) {
      pre = cur

      fn.apply(this, args)
    }
  }
}

/**
 * 判断数组数据中的key判断是否相同
 * @param {string} key 需要判断的key
 * @returns {function}
 * @example
 *  const isIdEqualed = createEqualFromKey('id')
 *  isIdEqualed([{ id: 1 }, { id: 2 }]) => false
 */
export function createEqualFromKey(key) {
  return function (list = []) {
    return !list.some(item => list.some(t => t[key] !== item[key]))
  }
}

/**
 * 获取字符所占宽度
 * @param {string} str 字符
 * @param {number} enWidth 英文宽度
 * @param {number} cnWidth 中文宽度
 * @returns number
 * @example
 *  getWordWidth('123ab你好') => 68
 */
export const getWordWidth = (str = '', enWidth = 8, cnWidth = 13) => {
  str = str + '' // 将非字符转为字符串

  if (str.trim().length === 0) return 0

  const isZh = /[\u4e00-\u9fa5]/g
  let width = 0

  const matched = str.match(isZh) ?? []
  if (matched.length) {
    width += matched.length * cnWidth
  }

  width += (str.length - matched.length) * enWidth

  return width
}

export const getTextWidth = (str, fontSize = 14) => {
  const ele = document.createElement('span')
  ele.style.display = 'inline-block'
  ele.style.fontSize = fontSize + 'px'
  ele.innerHTML = str
  document.body.appendChild(ele)
  const width = ele.clientWidth
  document.body.removeChild(ele)
  return width
}

/**
 * 判断数组数据中的key判断是否相同
 * @param {String} key 需要判断的key
 * @returns Boolean
 */
export function createIsEqualFromKey(key) {
  return function (list = []) {
    return !list.some(item => list.some(t => t[key] !== item[key]))
  }
}

/**
 * 合并两个对象，给最后一个优先级
 * @param {Object} target
 * @param {(Object|Array)} source
 * @returns {Object}
 */
export function objectMerge(target, source) {
  if (typeof target !== 'object') {
    target = {}
  }
  if (Array.isArray(source)) {
    return source.slice()
  }
  Object.keys(source).forEach(property => {
    const sourceProperty = source[property]
    if (typeof sourceProperty === 'object') {
      target[property] = objectMerge(target[property], sourceProperty)
    } else {
      target[property] = sourceProperty
    }
  })
  return target
}

/**
 * 复制文本 + 副本 + n
 * @param  {string} str [原文本]
 * @param  {string} spe [拼接的符号]
 * @param  {string} joinStr [拼接的字符]
 * @param  {array} exited 已存在的名称
 * @return {string}     [str + spe + suffix + n]
 */
export function copyText(str = '', option = {}) {
  const { spe = '_', joinStr = '副本', exited = [] } = option
  const [suffix, ...prefix] = str.split(spe).reverse()

  let name = ''
  let resNum = 0

  if (!prefix.length) {
    name = suffix
  } else {
    if (isNaN(suffix)) {
      name = prefix.reverse().join(spe) + '_' + suffix
    } else {
      name = prefix.reverse().join(spe)
    }

    resNum = isNaN(suffix) ? 0 : suffix
  }

  const reg = new RegExp(`^${name + spe + joinStr}(\\d*)$`)
  // 已经存在的数字
  const exitedNumber = exited.filter(t => reg.test(t)).map(t => +t.match(reg)[1])
  // 如果已经存在数字，则取已存在的最大数字作为初始值
  const initNumber = Math.max(...exitedNumber, resNum)

  if (exitedNumber.length) {
    resNum = initNumber
  }

  resNum++

  return name + spe + joinStr + resNum
}

/**
 * 根据排序字段创建排序方法
 * @param {boolean} isUp 是否升序
 * @param {string|number} prop 排序字段
 * @returns
 */
export function createSortByOrder(isUp = false, prop) {
  const isDate = e => isNaN(e) && !isNaN(Date.parse(e))

  return function (a, b) {
    const aV = typeof prop !== 'undefined' ? a[prop] : a
    const bV = typeof prop !== 'undefined' ? b[prop] : b

    if (typeof aV === 'string') {
      if (isDate(aV)) {
        return isUp
          ? new Date(aV).getTime() - new Date(bV).getTime()
          : new Date(bV).getTime() - new Date(aV).getTime()
      } else {
        let aa = aV || '',
          bb = bV || ''
        return isUp ? aa.localeCompare(bb) : bb.localeCompare(aa)
      }
    } else {
      return isUp ? aV - bV : bV - aV
    }
  }
}

/**
 * 获取指定keys的对象
 * @param {T} obj
 * @param {string[]} keys
 * @returns {T}
 * @example getByInlcudesKeys({id: 1, age: 2, address: 3}, ['id', 'age']) => {id: 1, age: 2}
 */
export const getByInlcudesKeys = (obj = {}, keys = []) => {
  if (keys.length === 0) return obj

  return keys.reduce((acc, key) => ((acc[key] = obj[key]), acc), {})
}

/**
 * 获取非指定keys的对象
 * @param {T} obj
 * @param {string[]} keys
 * @returns {T}
 * @example getByExcludeKeys({id: 1, age: 2, address: 3}, ['id', 'age']) => {address: 3}
 */
export const getByExcludeKeys = (obj = {}, keys = []) => {
  if (keys.length === 0) return obj

  return keys.reduce((acc, key) => {
    if (key in acc) {
      delete obj[key]
    }

    return acc
  }, obj)
}

/**
 *
 * @param {TreeData} tree
 * @param {string} childrenKey
 * @returns {array}
 */
export function treeToList(tree = [], childrenKey = 'children') {
  return tree.slice().reduce((acc, pre) => {
    const children = pre[childrenKey] || []
    const res = children.length ? treeToList(children) : []

    return acc.concat({ ...pre, children: [] }).concat(res)
  }, [])
}

/**
 * 扁平数组转树
 * @param {array} list 数据源
 * @param {string} parentKey 父级key
 * @param {string} childKey 子级key
 * @returns
 */
export const listToTree = (list = [], parentKey = 'parentId', childKey = 'children') => {
  const res = []

  for (let i = 0; i < list.length; i++) {
    const item = list[i]
    const resItem = res.find(t => t.id === item.id)
    const parentItem = list.find(t => t.id === item[parentKey])

    if (!resItem) {
      if (!parentItem) {
        res.push(item)
      } else {
        if (typeof parentItem[childKey] === 'undefined') {
          parentItem[childKey] = []
        }

        parentItem[childKey].push(item)
      }
    }
  }

  return res
}

/**
 * 根据父级id，生成树形结构
 * @param {array} list
 * @param {number} parentId
 * @returns {TreeData}
 */
export function listToTreeByPrentId(list = [], parentId) {
  const children = list.filter(item => item.parentId === parentId)
  const others = list.filter(item => children.every(fst => fst.id !== item.id))

  return children.map(child => {
    return {
      ...child,
      children: listToTreeByPrentId(others, child.id),
    }
  })
}

/**
 * 树排序
 * @param {*} treeList 树列表
 * @param {*} sortKey 排序的字段
 * @returns
 */
export const sortTree = (treeList = [], sortKey = 'sort') => {
  return treeList
    .map(t => {
      const children = t.children || []

      return {
        ...t,
        children: sortTree(children),
      }
    })
    .sort((a, b) => a[sortKey] - b[sortKey])
}

/**
 * 函数柯里化
 * @param {*} fn
 * @param  {...any} args
 * @returns
 */
export function curry(fn) {
  const arity = fn.length
  return function curried(...args) {
    if (args.length >= arity) {
      return fn.apply(this, args)
    } else {
      return function (...rest) {
        return curried.apply(this, args.concat(rest))
      }
    }
  }
}
export const curried = function (fn, ...args) {
  return fn.length <= args.length ? fn(...args) : curry.bind(null, fn, ...args)
}
