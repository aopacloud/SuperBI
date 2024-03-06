import { COMPARE, CATEGORY } from '@/CONST.dict'
import { getWordWidth } from 'common/utils/help'
import { is_vs, transformFieldsByVs, formatFieldDisplay } from './index.js'
import { getCompareDisplay } from '../Table/utils.js'

/**
 *  求和
 * @param {array} list
 * @param {string} key
 * @returns
 */
const getSum = (list = [], key = '') =>
  list.reduce((a, b) => {
    a += b[key]

    return a
  }, 0)

/**
 * 获取汇总行数据
 * @param {{keys: array<string>, column: array<Column>, list: array<any>}} param0
 * @returns
 */
export const getSummary = ({ keys = [], columns = [], list = [] }) => {
  return keys.reduce((acc, key, i) => {
    const { field } = columns[i].params
    if (field.category !== CATEGORY.INDEX) return acc

    acc[key] = getSum(list, key)

    return acc
  }, {})
}

// 表头填充宽度 paddingLeft + paddingRight + sortIcon
export const CELL_HEADER_PADDING = 10 + 30 + 20
// 内容区填充宽度 paddingLeft + paddingRight
export const CELL_BODY_PADDING = 10 + 10
// 表格缓冲宽度
export const CELL_BUFFER_WIDTH = 10
// 单元格最小宽度
export const CELL_MIN_WIDTH = 100

/**
 *
 * @param {number} colIndex 当前列索引
 * @param {number} allCols 所有列索引
 * @param {array<number>} fixedColumnSpan 列冻结位置
 * @returns {''|'left'|'right'}
 */
export const getFixedPlace = (colIndex, allCols, fixedColumnSpan = []) => {
  const [start, end] = fixedColumnSpan

  if (isNaN(start)) {
    return ''
  } else {
    // 前几列 + 后几列 > 总列数 => 全部冻结
    if (start + end >= allCols) return 'left'
    // 当前列 <= 前几列 => 左冻结
    if (colIndex + 1 <= start) return 'left'
    // 总列数 - 当前列 = 剩余列 <= 后几列 => 右冻结
    if (allCols - colIndex <= end) return 'right'
  }
}

export default function createTableData({
  originFields = [],
  originData = [],
  datasetFields = [],
  compare,
  choosed = {},
  config = {},
}) {
  /**
   * 同环比配置
   * mode: 显示模式, 0 默认，1 差值， 2 差值百分比
   * merge: 合并显示, false 单独显示， true 合并显示
   */
  const { mode = COMPARE.MODE.ORIGIN, merge = COMPARE.MERGE.FALSE } = config.compare || {}

  // 对比字段加上后缀
  const fields = transformFieldsByVs({
    fields: originFields,
    compare,
    compareOption: config.compare,
  })

  let columns = fields.map((field, i) => {
    const { displayName, category, renderName, dataType, _isVs } = field
    // 默认插槽
    const slotDefault = dataType.includes('TIME') ? 'date' : _isVs ? 'vs' : category //'default'

    return {
      title: displayName,
      field: renderName,
      sortable: true,
      params: {
        field: {
          ...field,
        },
      },
      slots: {
        header: 'header',
        default: slotDefault,
        footer: i === 0 ? 'footer.summary' : 'footer.' + category,
      },
    }
  })

  const list = originData.map(arr => {
    return arr.reduce((acc, pre, i) => {
      const { field: renderName } = columns[i]

      acc[renderName] = pre

      return acc
    }, {})
  })

  // 对比 单独显示 则过滤掉过滤字段
  if (merge) {
    columns = columns.reduce((acc, cur, i) => {
      if (!is_vs(cur.field)) {
        acc.push(cur)
      } else {
        acc.pop()

        const { field } = cur.params
        const titleWidth = getWordWidth(cur.title) + CELL_HEADER_PADDING
        const maxContentWidth = Math.max(
          ...originData
            .map(t => {
              const targetValue = t[i - 1] // 前一个值为当前值
              const originValue = t[i] // '当前值' 为对比值

              const prevStr = formatFieldDisplay(targetValue, field, datasetFields)
              const nextStr =
                ' ( ' +
                getCompareDisplay(originValue, targetValue, mode)(field, datasetFields) +
                ' ) '

              return getWordWidth(prevStr) + getWordWidth(nextStr)
            })
            .flat()
        )

        acc.push({
          ...cur,
          _width: Math.max(
            Math.max(titleWidth, maxContentWidth + CELL_BODY_PADDING),
            CELL_MIN_WIDTH
          ),
        })
      }

      return acc
    }, [])
  }

  return {
    columns,
    list,
  }
}
