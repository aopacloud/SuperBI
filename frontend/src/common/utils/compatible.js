/**
 * Compatible for safari
 */

/**
 * findLast => Array.prototype.findLast
 * @param {*} list
 * @param {*} cb
 * @returns
 */
export const findLast = (list, cb) => {
  let index = list.length - 1
  while (index >= 0) {
    if (cb(list[index])) {
      return list[index]
    }
    index--
  }
  return null
}

/**
 * findLast => Array.prototype.findLastIndex
 * @param {*} list
 * @param {*} cb
 * @returns
 */
export const findLastIndex = (list, cb) => {
  let index = list.length - 1
  while (index >= 0) {
    if (cb(list[index])) {
      return index
    }
    index--
  }
  return -1
}
