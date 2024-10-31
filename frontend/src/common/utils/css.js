/**
 * CSS字符串转对象
 * @param {string} CSS字符串转
 * @return {object} CSS对象
 */
export const cssStrToObj = (str = '') => {
  if (!str) return {}

  return str.split(';').reduce((acc, item) => {
    const [key, value] = item.split(':')
    acc[key] = value

    return acc
  }, {})
}

/**
 * CSS对象转换成字符串
 * @param {object}  obj CSS对象
 * @returns {string} CSS字符串
 */
export const cssObjToStr = obj => {
  if (!obj) return ''

  let str = ''
  for (const key in obj) {
    str += `${key}: ${obj[key]};`
  }

  return str
}
