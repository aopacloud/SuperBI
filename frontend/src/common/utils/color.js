/*
 * @Author: huanghe
 * @Date:   2022-12-08 16:05:12
 * @Last Modified by:   huanghe
 * @Last Modified time: 2022-12-09 15:32:48
 */

// #xxxxxx 转 rgb
export function hex2Rgb(hex, o) {
  if (!hex) hex = '#2c4dae'
  // 转化为小写
  hex = hex.toLowerCase()

  const r = hex.slice(1, 3),
    g = hex.slice(3, 5),
    b = hex.slice(5, 7)

  const opacity = o > 1 ? 1 : o < 0 ? 0 : o
  const _opacity = o !== undefined ? ', ' + opacity : ''
  return `rgba(${parseInt('0x' + r)}, ${parseInt('0x' + g)}, ${parseInt(
    '0x' + b
  )}${_opacity})`
}

// rgb 转 #xxxxxx
export function rgb2hex(rgba) {
  const rgbs = rgba.match(/[0-255]{1,3}/g)
  const [r, g, b] = [...rgbs].map(t => {
    t = +t
    return t
  })
  const reg = /^\d{1,3}$/

  if (!reg.test(r) || !reg.test(g) || !reg.test(b)) {
    console.error('输入错误的rgb颜色值')

    return
  }

  const hexs = [r.toString(16), g.toString(16), b.toString(16)]

  for (let i = 0; i < 3; i++) {
    if (hexs[i].length == 1) {
      hexs[i] = '0' + hexs[i]
    }
  }

  return '#' + hexs.join('')
}

// 从 #xxxxxx 中获取 rgb 数组
function getRgbs(color) {
  const rgbs = hex2Rgb(color)
  const res = rgbs.match(/\d{1,3}/g)
  return [...res].slice(0, 3).map(t => {
    t = +t
    return t
  })
}

// 深化\淡化颜色 #xxxxxx
export function darkenColor(color, step = 0) {
  const rgbs = getRgbs(color)
  const [r, g, b] = rgbs

  // RGB 差值
  const product = 0.6
  const sR = 0.75 * product
  const sG = 24 * product
  const sB = 27 * product

  return (
    'rgb(' +
    parseInt(sR * step + r) +
    ',' +
    parseInt(sG * step + g) +
    ',' +
    parseInt(sB * step + b) +
    ')'
  )
}

export function lightenColor(color = '#000000', opacity = 1) {
  opacity = opacity > 1 ? 1 : opacity < 0 ? 0 : opacity

  color = color.replace(/\#/g, '').toUpperCase()

  if (color.length === 3) {
    let arr = color.split('')
    color = ''
    for (let i = 0; i < arr.length; i++) {
      color += arr[i] + arr[i] //将简写的3位字符补全到6位字符
    }
  }
  let num = Math.round(255 * opacity) //四舍五入
  let str = ''
  let arrHex = [
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
    'A',
    'B',
    'C',
    'D',
    'E',
    'F',
  ] //十六进制数组
  while (num > 0) {
    let mod = num % 16
    num = (num - mod) / 16
    str = arrHex[mod] + str
  }
  if (str.length == 1) str = '0' + str
  if (str.length == 0) str = '00'
  return `#${color + str}`
}

// hex颜色转rgb颜色
export function hexToRgb(str) {
  str = str.replace('#', '')
  let hexs = str.match(/../g)
  for (let i = 0; i < 3; i++) {
    hexs[i] = parseInt(hexs[i], 16)
  }
  return hexs
}

// rgb颜色转Hex颜色
export function rgbToHex(r, g, b) {
  let hexs = [r.toString(16), g.toString(16), b.toString(16)]
  for (let i = 0; i < 3; i++) {
    if (hexs[i].length == 1) {
      hexs[i] = `0${hexs[i]}`
    }
  }
  return `#${hexs.join('')}`
}

// 变浅颜色值
export function getLightColor(color, level) {
  let rgb = hexToRgb(color)
  for (let i = 0; i < 3; i++) {
    rgb[i] = Math.floor((255 - rgb[i]) * level + rgb[i])
  }
  return rgbToHex(rgb[0], rgb[1], rgb[2])
}

// 变深颜色值
export function getDarkColor(color, level) {
  let rgb = hexToRgb(color)
  for (let i = 0; i < 3; i++) {
    rgb[i] = Math.floor(rgb[i] * (1 - level))
  }
  return rgbToHex(rgb[0], rgb[1], rgb[2])
}
