import { LayoutOptions } from './config'
import { getRandomKey } from 'common/utils/string'

const { colNum, rowHeight, margin } = LayoutOptions

// 根据真实高度获取GridLayoutItem所需高度（组件要去必须为自然数）
export const getLayoutItemHByRealHeight = (heightPx = 0) => {
  let i = 1,
    height = 0

  while (height < heightPx) {
    height = i * rowHeight + margin[0] * (i - 1)
    i += 1
  }

  return i - 1
}

/**
 * 获取 gridLayoutItem 尺寸
 * @param {string} type 宽\高
 * @param {string} size 尺寸
 * @returns {number}
 */
export const getLayoutItemSize = (type, size = 'default') => {
  const SIZE_MAP = {
    width: {
      large: colNum,
      middle: colNum / 2,
      small: colNum / 4,
      default: colNum / 2,
    },
    height: {
      large: rowHeight * 36,
      middle: rowHeight * 36,
      small: rowHeight * 18,
      default: rowHeight * 36,
    },
  }

  const sizes = SIZE_MAP[type]

  return sizes[size] ?? sizes['default']
}

/**
 * 获取 gridLayoutItem height
 * @param {string} type 看板项类型
 * @param {string} size 尺寸
 * @returns {number}
 */
export const getLayoutItemHeight = (type, size) => {
  if (type === 'REMARK' || type === 'FILTER') {
    return getLayoutItemSize('height', 'small')
  } else {
    return getLayoutItemSize('height', size)
  }
}

/**
 * 转换 GridLayout 参数
 * @param {DashboardComponentItem} item 看板项
 */
export const transformGridLayoutItem = item => {
  const { layout, content, ...res } = item

  const layoutObj = typeof layout === 'string' ? JSON.parse(layout) : layout
  const contentObj = typeof content === 'string' ? JSON.parse(content) : content

  const i = getRandomKey()

  delete item.layout
  delete item.content

  return {
    ...res,
    ...layoutObj, // 展开 layoutStr
    _id: i,
    i,
    _loaded: false,
    content: contentObj,
  }
}

// 历史数据宽度对照表
export const widthCompatibleMap = { 5: 'small', 10: 'middle', 20: 'large' }
// 历史数据高度对照表
export const heighCompatibletMap = { 5: 'small', 10: 'large' }

export const compatibleGridLayout = list => {
  const result = []

  for (let i = 0; i < list.length; i++) {
    const item = list[i]

    if (!result.length) {
      result.push(item)

      continue
    }

    const preItem = list[i - 1]

    const { x: pX, y: pY, w: pW, h: pH } = preItem
    const { x, y, w, h } = item

    if (y === pY) {
      if (w + pX + pW <= colNum) {
        item.x = pX + pW
        item.y = y
      } else {
        item.x = 0
        item.y = pY + pH
      }
      result.push(item)
    } else {
      result.push(item)
    }
  }

  return result
}

/**
 * 兼容旧版 gridLayoutItem 数据
 * @param {LayoutItem} item GridLayout 布局数据
 */
export const compatibleGridLayoutItem = item => {
  const { type, w, h, _size, ...res } = item

  let _w = w,
    _h = h,
    __size = _size

  if (type === 'REMARK' || type === 'FILTER') {
    _w = getLayoutItemSize('width', 'large')
    _h = getLayoutItemSize('height', 'small')
  } else {
    // 没有 _size 则为历史数据
    if (!_size) {
      __size = widthCompatibleMap[w]
      _w = getLayoutItemSize('width', __size)
      const _hSize = heighCompatibletMap[h]
      if (_hSize) {
        _h = getLayoutItemSize('height', _hSize)
      } else {
        const RATIO = 10
        // 自定义w的高度，
        const heightDis = (h * 25) / RATIO,
          marginDis = (heightDis * 5) / RATIO

        _h = Math.max(heightDis + marginDis, getLayoutItemSize('height', __size))
      }
    }
  }

  return {
    ...res,
    type,
    w: _w,
    h: _h,
    _size: __size,
  }
}
