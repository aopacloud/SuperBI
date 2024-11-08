export const getPrimitiveType = o =>
  Object.prototype.toString.call(o).slice(8, -1).toLowerCase()

export const isEmpty = o => {
  if (getPrimitiveType(o) === 'undefined' || getPrimitiveType(o) === 'null')
    return true

  if (getPrimitiveType(o) === 'string') return o.length === 0

  if (getPrimitiveType(o) === 'number') return isNaN(o)

  if (getPrimitiveType(o) === 'array') return o.length === 0

  if (getPrimitiveType(o) === 'object') return Object.keys(o).length === 0

  return false
}

export const isObject = o => getPrimitiveType(o) === 'object'

/**
 * 递归合并对象，如果目标对象属性为对象，则递归合并
 * @param {T} target 目标对象
 * @param {T} source 合并的对象
 * @returns {T}
 */
export const merge = (target, source) => {
  if (isEmpty(target)) return source

  if (isEmpty(source)) return target

  for (const key in source) {
    if (source.hasOwnProperty(key)) {
      if (getPrimitiveType(source[key]) === 'object') {
        target[key] = merge(target[key], source[key])
      } else {
        target[key] = source[key]
      }
    }
  }

  return target
}

/**
 * 深度合并两个对象
 *
 * @param target 目标对象
 * @param source 源对象
 * @returns 合并后的对象
 */
export const mergeDeep = (target, source) => {
  let output = Object.assign({}, target)
  if (isObject(target) && isObject(source)) {
    Object.keys(source).forEach(key => {
      if (isObject(source[key])) {
        if (!(key in target)) Object.assign(output, { [key]: source[key] })
        else output[key] = mergeDeep(target[key], source[key])
      } else {
        Object.assign(output, { [key]: source[key] })
      }
    })
  }
  return output
}
