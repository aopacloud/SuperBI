/**
 * 数字千分位处理
 * @param {number|string} num 原值
 * @returns {string}
 * @example
 *  toThousand(2.3) => 2.3
 *  toThousand(1232) => 1,232
 */
export function toThousand(num) {
  if (isNaN(num)) return num

  const [n1, n2] = (num + '').split('.')
  return n1.replace(/\B(?=(\d{3})+(?!\d))/g, ',') + (n2 ? '.' + n2 : '')
}

/**
 * 数字保留小数位
 * @param  {number|string} num      原值
 * @param  {number} digit    小数位
 * @param  {number} thousand 千分位
 * @return {number|string}
 * @example
 *  toDigit(2.3, 2) => 2.30
 *  toDigit(1232.3, 2, true) => 1,232.30
 */
export function toDigit(num, digit = 0, thousand = true) {
  if (isNaN(num)) return num

  const n = Number(num).toFixed(digit)

  return thousand ? toThousand(n) : n
}

/**
 * 数字百分比保留小数位
 * @param  {number|string} num      原值
 * @param  {number} digit    小数位
 * @param  {number} thousand 千分位
 * @return {string}
 * @example
 *  toPercent(2.3, 2) => 22.30%
 *  toPercent(1232, 2, true) => 1,232.00%
 */
export function toPercent(num, digit = 2, thousand) {
  if (isNaN(num)) return num
  if (digit < 0) digit = 0

  const n = (num * 100).toFixed(digit)

  return thousand ? toThousand(n) + '%' : n + '%'
}
