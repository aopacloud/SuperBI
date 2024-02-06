export const getPrimitiveType = o => Object.prototype.toString.call(o).slice(8, -1)

export const isEmpty = o => {
  if (getPrimitiveType(o) === 'undefiend' || getPrimitiveType(o) === 'null') return true

  if (getPrimitiveType(o) === 'string') return o.length === 0

  if (getPrimitiveType(o) === 'number') return isNaN(o)

  if (getPrimitiveType(o) === 'array') return o.length === 0

  if (getPrimitiveType(o) === 'object') return Object.keys(o).length === 0

  return false
}

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
