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
export function uuid() {
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
 * 首字符大写
 * @param {*} str 字符
 * @returns
 * @example upcaseFirst('abCdE') => 'Abcde'
 */
export const upcaseFirst = str => {
  const [r, ...res] = str

  return r.toUpperCase() + res.join('').toLowerCase()
}

/**
 * 转大写驼峰
 * @param {string} str 字符
 * @param {string} split 分隔符
 * @returns
 * @example camelCase('aa_bb_cc') => 'AbBbCc'
 */
export const camelCase = (str = '', split = '_') => {
  return str.split(split).map(upcaseFirst).join('')
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

/**
 * 将数字索引转换为大写字母字符串
 * @param {number } i 数字索引，默认为0
 * @returns {string} 返回对应的大写字母字符串
 */
export const transformIndexToUpcaseWord = (i = 0) => {
  i = i < 0 ? 1 : i + 1

  let result = ''
  while (i > 0) {
    i--
    result = String.fromCharCode((i % 26) + 'A'.charCodeAt(0)) + result
    i = Math.floor(i / 26)
  }

  return result
}
