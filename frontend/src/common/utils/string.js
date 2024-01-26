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
